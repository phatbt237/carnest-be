package com.example.carnest.Service;

import com.example.carnest.Entity.*;
import com.example.carnest.Enum.*;
import com.example.carnest.Exception.BadRequestException;
import com.example.carnest.Exception.ResourceNotFoundException;
import com.example.carnest.Model.AuctionDTO;
import com.example.carnest.Model.ShopDTO;
import com.example.carnest.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuctionService {

    private final AuctionRepository auctionRepository;
    private final AuctionBidRepository bidRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final AuctionWebSocketService webSocketService;

    private static final int MAX_SIZE = 50;

    @Autowired
    public AuctionService(AuctionRepository auctionRepository, AuctionBidRepository bidRepository,
                          ProductRepository productRepository, ProductImageRepository productImageRepository,
                          ShopRepository shopRepository, UserRepository userRepository, AuctionWebSocketService webSocketService) {
        this.auctionRepository = auctionRepository;
        this.bidRepository = bidRepository;
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
        this.shopRepository = shopRepository;
        this.userRepository = userRepository;
        this.webSocketService = webSocketService;
    }

    // ===== TẠO AUCTION =====
    @Transactional
    public AuctionDTO.AuctionResponse create(Long userId, AuctionDTO.CreateRequest request) {
        Shop shop = shopRepository.findByUserIdWithUser(userId)
                .orElseThrow(() -> new BadRequestException("Bạn chưa có shop"));

        Product product = productRepository.findByIdForUpdate(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));

        if (!product.getShop().getId().equals(shop.getId())) {
            throw new BadRequestException("Sản phẩm này không thuộc shop của bạn");
        }
        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new BadRequestException("Sản phẩm không ở trạng thái có thể đấu giá");
        }
        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new BadRequestException("Thời gian kết thúc phải sau thời gian bắt đầu");
        }

        // Đổi product sang RESERVED
        product.setStatus(ProductStatus.RESERVED);
        productRepository.save(product);

        Auction auction = new Auction();
        auction.setProduct(product);
        auction.setSeller(shop.getUser());
        auction.setStartingPrice(request.getStartingPrice());
        auction.setBidIncrement(request.getBidIncrement());
        auction.setReservePrice(request.getReservePrice());
        auction.setBuyNowPrice(request.getBuyNowPrice());
        auction.setStartTime(request.getStartTime());
        auction.setEndTime(request.getEndTime());
        auction.setAutoExtendMinutes(request.getAutoExtendMinutes() != null ? request.getAutoExtendMinutes() : 3);
        auction.setSnipeThresholdMin(request.getSnipeThresholdMin() != null ? request.getSnipeThresholdMin() : 2);
        auction.setCurrentPrice(request.getStartingPrice());
        auction.setTotalBids(0);
        auction.setExtendedCount(0);

        // Nếu startTime <= now → ACTIVE, ngược lại → UPCOMING
        if (request.getStartTime().isBefore(LocalDateTime.now()) || request.getStartTime().isEqual(LocalDateTime.now())) {
            auction.setStatus(AuctionStatus.ACTIVE);
        } else {
            auction.setStatus(AuctionStatus.UPCOMING);
        }

        auction = auctionRepository.save(auction);
        return toAuctionResponse(auction);
    }

    // ===== ĐẶT BID =====
    @Transactional
    public AuctionDTO.AuctionResponse placeBid(Long userId, Long auctionId, AuctionDTO.BidRequest request) {
        Auction auction = auctionRepository.findByIdFull(auctionId)
                .orElseThrow(() -> new ResourceNotFoundException("Auction", "id", auctionId));

        if (auction.getStatus() != AuctionStatus.ACTIVE) {
            throw new BadRequestException("Phiên đấu giá chưa bắt đầu hoặc đã kết thúc");
        }
        if (auction.getSeller().getId().equals(userId)) {
            throw new BadRequestException("Bạn không thể bid sản phẩm của chính mình");
        }
        if (auction.getEndTime().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Phiên đấu giá đã hết thời gian");
        }

        BigDecimal minBid = auction.getCurrentPrice().add(auction.getBidIncrement());
        if (auction.getTotalBids() == 0) {
            minBid = auction.getStartingPrice(); // Bid đầu tiên chỉ cần >= startingPrice
        }

        if (request.getBidAmount().compareTo(minBid) < 0) {
            throw new BadRequestException("Giá bid tối thiểu là " + minBid + " VNĐ");
        }

        // Buy now
        if (auction.getBuyNowPrice() != null && request.getBidAmount().compareTo(auction.getBuyNowPrice()) >= 0) {
            return executeBuyNow(userId, auction, request.getBidAmount());
        }

        // Đánh dấu bid cũ không winning nữa
        bidRepository.findWinningBid(auctionId).ifPresent(oldBid -> {
            oldBid.setIsWinning(false);
            bidRepository.save(oldBid);
        });

        // Tạo bid mới
        User bidder = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        AuctionBid bid = new AuctionBid();
        bid.setAuction(auction);
        bid.setBidder(bidder);
        bid.setBidAmount(request.getBidAmount());
        bid.setIsAutoBid(request.getMaxAutoBid() != null);
        bid.setMaxAutoBid(request.getMaxAutoBid());
        bid.setIsWinning(true);
        bidRepository.save(bid);

        // Gửi thông báo bid mới qua WebSocket
        webSocketService.sendNewBid(auctionId, bidder.getUsername(),
                request.getBidAmount(), false);

        // Cập nhật auction
        auction.setCurrentPrice(request.getBidAmount());
        auction.setTotalBids(auction.getTotalBids() + 1);
        auction.setWinner(bidder);
        auction.setWinningBid(bid);

        // Anti-snipe: gia hạn nếu bid trong X phút cuối
        long minutesLeft = ChronoUnit.MINUTES.between(LocalDateTime.now(), auction.getEndTime());
        if (minutesLeft <= auction.getSnipeThresholdMin()) {
            auction.setEndTime(auction.getEndTime().plusMinutes(auction.getAutoExtendMinutes()));
            auction.setExtendedCount(auction.getExtendedCount() + 1);
        }

        auctionRepository.save(auction);

        sendAuctionUpdate(auction);

        // Auto-bid: kiểm tra có ai đặt maxAutoBid cao hơn không
        processAutoBid(auction, bid);
        return toAuctionResponse(auction);
    }

    // ===== MUA NGAY (BUY NOW) =====
    private AuctionDTO.AuctionResponse executeBuyNow(Long userId, Auction auction, BigDecimal amount) {
        User bidder = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        bidRepository.findWinningBid(auction.getId()).ifPresent(oldBid -> {
            oldBid.setIsWinning(false);
            bidRepository.save(oldBid);
        });

        AuctionBid bid = new AuctionBid();
        bid.setAuction(auction);
        bid.setBidder(bidder);
        bid.setBidAmount(amount);
        bid.setIsWinning(true);
        bidRepository.save(bid);

        auction.setCurrentPrice(amount);
        auction.setTotalBids(auction.getTotalBids() + 1);
        auction.setWinner(bidder);
        auction.setWinningBid(bid);
        auction.setStatus(AuctionStatus.ENDED);
        auction.setEndTime(LocalDateTime.now());
        auctionRepository.save(auction);

        return toAuctionResponse(auction);
    }

    // ===== HỦY AUCTION (seller) =====
    @Transactional
    public AuctionDTO.AuctionResponse cancel(Long userId, Long auctionId) {
        Auction auction = auctionRepository.findByIdFull(auctionId)
                .orElseThrow(() -> new ResourceNotFoundException("Auction", "id", auctionId));

        if (!auction.getSeller().getId().equals(userId)) {
            throw new BadRequestException("Bạn không có quyền hủy phiên này");
        }
        if (auction.getStatus() != AuctionStatus.UPCOMING && auction.getStatus() != AuctionStatus.ACTIVE) {
            throw new BadRequestException("Không thể hủy phiên ở trạng thái " + auction.getStatus());
        }
        if (auction.getTotalBids() > 0) {
            throw new BadRequestException("Không thể hủy phiên đã có người bid");
        }

        auction.setStatus(AuctionStatus.CANCELLED);
        auctionRepository.save(auction);

        // Hoàn product về ACTIVE
        Product p = auction.getProduct();
        p.setStatus(ProductStatus.ACTIVE);
        productRepository.save(p);

        return toAuctionResponse(auction);
    }

    // ===== XEM CHI TIẾT =====
    public AuctionDTO.AuctionResponse getById(Long id) {
        Auction auction = auctionRepository.findByIdFull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Auction", "id", id));
        return toAuctionResponse(auction);
    }

    // ===== DANH SÁCH AUCTION =====
    public ShopDTO.CursorPage<AuctionDTO.AuctionResponse> getAuctions(
            String filter, String cursor, int size) {
        size = Math.min(Math.max(size, 1), MAX_SIZE);
        Long cursorId = (cursor != null && !cursor.isEmpty()) ? Long.parseLong(cursor) : null;

        List<Auction> auctions;
        AuctionStatus status = AuctionStatus.ACTIVE;

        if ("ending_soon".equals(filter)) {
            auctions = auctionRepository.findEndingSoon(
                    LocalDateTime.now().plusHours(1), cursorId, size + 1);
        } else if ("upcoming".equals(filter)) {
            status = AuctionStatus.UPCOMING;
            auctions = auctionRepository.findByStatus(status, cursorId, size + 1);
        } else if ("ended".equals(filter)) {
            status = AuctionStatus.ENDED;
            auctions = auctionRepository.findByStatus(status, cursorId, size + 1);
        } else {
            auctions = auctionRepository.findByStatus(status, cursorId, size + 1);
        }

        boolean hasMore = auctions.size() > size;
        if (hasMore) auctions = auctions.subList(0, size);

        List<AuctionDTO.AuctionResponse> items = auctions.stream()
                .map(this::toAuctionResponse).collect(Collectors.toList());

        String nextCursor = hasMore && !auctions.isEmpty()
                ? String.valueOf(auctions.get(auctions.size() - 1).getId()) : null;
        Long total = auctionRepository.countByStatus(status);

        return new ShopDTO.CursorPage<>(items, nextCursor, hasMore, items.size(), total);
    }

    // ===== HELPER =====
    private AuctionDTO.AuctionResponse toAuctionResponse(Auction a) {
        AuctionDTO.AuctionResponse r = new AuctionDTO.AuctionResponse();
        r.setId(a.getId());
        r.setStatus(a.getStatus().name());
        r.setStartingPrice(a.getStartingPrice());
        r.setCurrentPrice(a.getCurrentPrice());
        r.setBidIncrement(a.getBidIncrement());
        r.setBuyNowPrice(a.getBuyNowPrice());
        r.setHasReservePrice(a.getReservePrice() != null);
        r.setReserveMet(a.getReservePrice() != null && a.getCurrentPrice().compareTo(a.getReservePrice()) >= 0);
        r.setTotalBids(a.getTotalBids());
        r.setStartTime(a.getStartTime());
        r.setEndTime(a.getEndTime());
        r.setAutoExtendMinutes(a.getAutoExtendMinutes());
        r.setExtendedCount(a.getExtendedCount());
        r.setCreatedAt(a.getCreatedAt());

        // Product
        Product p = a.getProduct();
        AuctionDTO.ProductSummary ps = new AuctionDTO.ProductSummary();
        ps.setId(p.getId());
        ps.setName(p.getName());
        ps.setSlug(p.getSlug());
        ps.setShopName(p.getShop().getShopName());
        ps.setShopSlug(p.getShop().getSlug());

        List<ProductImage> images = productImageRepository.findPrimaryByProductIds(List.of(p.getId()));
        if (!images.isEmpty()) ps.setPrimaryImage(images.get(0).getImageUrl());
        r.setProduct(ps);

        // Recent bids
        List<AuctionBid> bids = bidRepository.findByAuctionIdWithBidder(a.getId());
        r.setRecentBids(bids.stream().limit(10).map(this::toBidInfo).collect(Collectors.toList()));

        // Winning bid
        if (a.getWinningBid() != null) {
            r.setWinningBid(toBidInfo(a.getWinningBid()));
        }

        return r;
    }

    private AuctionDTO.BidInfo toBidInfo(AuctionBid b) {
        AuctionDTO.BidInfo info = new AuctionDTO.BidInfo();
        info.setId(b.getId());
        info.setBidderUsername(b.getBidder().getUsername());
        info.setBidAmount(b.getBidAmount());
        info.setIsWinning(b.getIsWinning());
        info.setCreatedAt(b.getCreatedAt());
        return info;
    }

    private void processAutoBid(Auction auction, AuctionBid newBid) {
        // Tìm tất cả bid có maxAutoBid, trừ người vừa bid
        List<AuctionBid> autoBids = bidRepository.findByAuctionIdWithBidder(auction.getId())
                .stream()
                .filter(b -> b.getMaxAutoBid() != null
                        && !b.getBidder().getId().equals(newBid.getBidder().getId())
                        && b.getMaxAutoBid().compareTo(newBid.getBidAmount()) > 0)
                .sorted((a, b) -> b.getMaxAutoBid().compareTo(a.getMaxAutoBid())) // cao nhất trước
                .collect(Collectors.toList());

        if (autoBids.isEmpty()) return;

        AuctionBid topAutoBid = autoBids.get(0);
        BigDecimal autoBidAmount = newBid.getBidAmount().add(auction.getBidIncrement());

        // Không vượt quá maxAutoBid
        if (autoBidAmount.compareTo(topAutoBid.getMaxAutoBid()) > 0) {
            autoBidAmount = topAutoBid.getMaxAutoBid();
        }

        // Phải cao hơn giá hiện tại + increment
        BigDecimal minRequired = auction.getCurrentPrice().add(auction.getBidIncrement());
        if (autoBidAmount.compareTo(minRequired) < 0) return;

        // Đánh dấu bid cũ không winning
        newBid.setIsWinning(false);
        bidRepository.save(newBid);

        // Tạo auto bid
        AuctionBid auto = new AuctionBid();
        auto.setAuction(auction);
        auto.setBidder(topAutoBid.getBidder());
        auto.setBidAmount(autoBidAmount);
        auto.setIsAutoBid(true);
        auto.setMaxAutoBid(topAutoBid.getMaxAutoBid());
        auto.setIsWinning(true);
        bidRepository.save(auto);

        // Cập nhật auction
        auction.setCurrentPrice(autoBidAmount);
        auction.setTotalBids(auction.getTotalBids() + 1);
        auction.setWinner(topAutoBid.getBidder());
        auction.setWinningBid(auto);
        auctionRepository.save(auction);

        webSocketService.sendNewBid(auction.getId(), topAutoBid.getBidder().getUsername(),
                autoBidAmount, true);
        webSocketService.sendUpdate(auction);

        sendAuctionUpdate(auction);
    }

    private void sendAuctionUpdate(Auction auction) {
        webSocketService.sendUpdate(auction);
    }
}