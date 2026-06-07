package com.example.carnest.Service;

import com.example.carnest.Entity.*;
import com.example.carnest.Enum.*;
import com.example.carnest.Exception.BadRequestException;
import com.example.carnest.Exception.ResourceNotFoundException;
import com.example.carnest.Model.AuctionDTO;
import com.example.carnest.Model.EventDTO;
import com.example.carnest.Model.ShopDTO;
import com.example.carnest.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private final EventPublisher eventPublisher;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;

    private static final int MAX_SIZE = 50;

    @Autowired
    public AuctionService(AuctionRepository auctionRepository, AuctionBidRepository bidRepository,
                          ProductRepository productRepository, ProductImageRepository productImageRepository,
                          ShopRepository shopRepository, UserRepository userRepository,
                          AuctionWebSocketService webSocketService, EventPublisher eventPublisher,
                          OrderRepository orderRepository, OrderItemRepository orderItemRepository,
                          OrderStatusHistoryRepository orderStatusHistoryRepository) {
        this.auctionRepository = auctionRepository;
        this.bidRepository = bidRepository;
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
        this.shopRepository = shopRepository;
        this.userRepository = userRepository;
        this.webSocketService = webSocketService;
        this.eventPublisher = eventPublisher;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderStatusHistoryRepository = orderStatusHistoryRepository;
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
        eventPublisher.publishAuctionClose(auction.getId(), auction.getEndTime());
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
            eventPublisher.publishAuctionClose(auction.getId(), auction.getEndTime());
        }

        auctionRepository.save(auction);

        // Publish bid event async
        EventDTO.AuctionBidEvent bidEvent = new EventDTO.AuctionBidEvent();
        bidEvent.setAuctionId(auctionId);
        bidEvent.setBidderId(userId);
        bidEvent.setBidderUsername(bidder.getUsername());
        bidEvent.setBidAmount(request.getBidAmount());
        bidEvent.setAutoBid(false);
        // Lấy previous winner để notify outbid
        AuctionBid previousWinning = bidRepository.findByAuctionIdWithBidder(auctionId).stream()
                .filter(b -> !b.getId().equals(bid.getId()) && b.getBidder() != null)
                .findFirst().orElse(null);
        if (previousWinning != null) {
            bidEvent.setPreviousWinnerId(previousWinning.getBidder().getId());
        }
        eventPublisher.publishAuctionBid(bidEvent);

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

    // ===== ĐÓNG AUCTION KHI HẾT GIỜ (gọi từ EventConsumer) =====
    @Transactional
    public void closeExpiredAuction(Long auctionId) {
        Auction auction = auctionRepository.findByIdForClose(auctionId).orElse(null);
        if (auction == null || auction.getStatus() != AuctionStatus.ACTIVE) return;
        // Nếu 2 message cùng đến, cái thứ 2 sẽ thấy version không khớp và bỏ qua

        // Anti-snipe đã extend endTime → delayed message cũ đến sớm, bỏ qua
        if (auction.getEndTime().isAfter(LocalDateTime.now())) {
            eventPublisher.publishAuctionClose(auctionId, auction.getEndTime());
            return;
        }

        if (auction.getWinner() != null) {
            if (auction.getReservePrice() != null &&
                    auction.getCurrentPrice().compareTo(auction.getReservePrice()) < 0) {
                auction.setStatus(AuctionStatus.NO_SALE);
                auction.getProduct().setStatus(ProductStatus.ACTIVE);
                productRepository.save(auction.getProduct());
            } else {
                auction.setStatus(AuctionStatus.ENDED);
                createOrderFromAuction(auction);
            }
        } else {
            auction.setStatus(AuctionStatus.NO_SALE);
            auction.getProduct().setStatus(ProductStatus.ACTIVE);
            productRepository.save(auction.getProduct());
        }

        auctionRepository.save(auction);
        webSocketService.sendAuctionEnded(auction);
        System.out.println("[AuctionService] Closed auction #" + auctionId + " → " + auction.getStatus());
    }

    private void createOrderFromAuction(Auction auction) {
        Product product = auction.getProduct();
        BigDecimal winPrice = auction.getCurrentPrice();

        Order order = new Order();
        order.setOrderCode("AUC-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-" + String.format("%04d", new java.util.Random().nextInt(10000)));
        order.setBuyer(auction.getWinner());
        order.setShop(product.getShop());
        order.setShippingName("");
        order.setShippingPhone("");
        order.setShippingAddress("");
        order.setSubtotal(winPrice);
        order.setShippingFee(BigDecimal.ZERO);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setTotalAmount(winPrice);
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setEscrowStatus(EscrowStatus.NONE);
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        order.setPaymentDeadline(LocalDateTime.now().plusMinutes(30));
        order.setBuyerNote("Đơn từ đấu giá #" + auction.getId());
        order = orderRepository.save(order);

        List<ProductImage> imgs = productImageRepository.findPrimaryByProductIds(List.of(product.getId()));

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProduct(product);
        item.setProductName(product.getName());
        item.setProductImage(imgs.isEmpty() ? null : imgs.get(0).getImageUrl());
        item.setPrice(winPrice);
        item.setQuantity(1);
        orderItemRepository.save(item);

        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrder(order);
        history.setToStatus(OrderStatus.PENDING_PAYMENT.name());
        history.setNote("Đơn từ đấu giá — giá thắng " + winPrice + " VNĐ — thanh toán trong 30 phút");
        orderStatusHistoryRepository.save(history);

        long expireDelayMs = 30 * 60 * 1000L;
        eventPublisher.publishOrderExpire(order.getId(), expireDelayMs);

        System.out.println("[AuctionService] Tạo order " + order.getOrderCode()
                + " cho winner " + auction.getWinner().getUsername());
    }

    // ===== SELLER: danh sách phiên đấu giá của mình =====
    public ShopDTO.CursorPage<AuctionDTO.SellerAuctionItem> getMyAuctions(
            Long sellerId, String statusFilter, String cursor, int size) {
        size = Math.min(Math.max(size, 1), MAX_SIZE);
        Long cursorId = (cursor != null && !cursor.isEmpty()) ? Long.parseLong(cursor) : null;
        AuctionStatus status = null;
        if (statusFilter != null && !statusFilter.isEmpty()) {
            try { status = AuctionStatus.valueOf(statusFilter.toUpperCase()); }
            catch (IllegalArgumentException ignored) {}
        }

        List<Object[]> rows = auctionRepository.findSellerAuctionItems(sellerId, status, cursorId);
        boolean hasMore = rows.size() > size;
        if (hasMore) rows = rows.subList(0, size);

        List<AuctionDTO.SellerAuctionItem> items = rows.stream().map(row -> {
            AuctionDTO.SellerAuctionItem item = new AuctionDTO.SellerAuctionItem();
            item.setId((Long) row[0]);
            item.setStatus(((AuctionStatus) row[1]).name());
            item.setProductId((Long) row[2]);
            item.setProductName((String) row[3]);
            item.setProductImage((String) row[4]);
            item.setStartingPrice((java.math.BigDecimal) row[5]);
            item.setCurrentPrice((java.math.BigDecimal) row[6]);
            item.setReservePrice((java.math.BigDecimal) row[7]);
            item.setReserveMet(row[7] != null &&
                    ((java.math.BigDecimal) row[6]).compareTo((java.math.BigDecimal) row[7]) >= 0);
            item.setTotalBids((Integer) row[8]);
            item.setStartTime((LocalDateTime) row[9]);
            item.setEndTime((LocalDateTime) row[10]);
            item.setExtendedCount((Integer) row[11]);
            item.setWinnerUsername((String) row[12]);
            item.setCreatedAt((LocalDateTime) row[13]);
            return item;
        }).collect(Collectors.toList());

        String nextCursor = hasMore ? String.valueOf((Long) rows.get(rows.size() - 1)[0]) : null;
        return new ShopDTO.CursorPage<>(items, nextCursor, hasMore, items.size(), (long) items.size());
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
        // Tìm auto-bidder tốt nhất từ người khác có maxAutoBid > bidAmount vừa bid
        List<AuctionBid> autoBids = bidRepository.findByAuctionIdWithBidder(auction.getId())
                .stream()
                .filter(b -> b.getMaxAutoBid() != null
                        && !b.getBidder().getId().equals(newBid.getBidder().getId())
                        && b.getMaxAutoBid().compareTo(newBid.getBidAmount()) > 0)
                .sorted((a, b) -> b.getMaxAutoBid().compareTo(a.getMaxAutoBid()))
                .collect(Collectors.toList());

        if (autoBids.isEmpty()) return;

        AuctionBid topAutoBid = autoBids.get(0);

        BigDecimal autoBidAmount;
        User winner;
        BigDecimal winnerMaxAutoBid;

        if (newBid.getMaxAutoBid() != null && newBid.getMaxAutoBid().compareTo(topAutoBid.getMaxAutoBid()) > 0) {
            // newBid có maxAutoBid cao hơn → newBid thắng, giá = maxAutoBid đối thủ + increment
            autoBidAmount = topAutoBid.getMaxAutoBid().add(auction.getBidIncrement());
            winner = newBid.getBidder();
            winnerMaxAutoBid = newBid.getMaxAutoBid();
        } else {
            // topAutoBid thắng (maxAutoBid cao hơn hoặc bằng)
            // giá = maxAutoBid của newBid + increment (nếu có), hoặc bidAmount + increment
            BigDecimal loserCeiling = newBid.getMaxAutoBid() != null ? newBid.getMaxAutoBid() : newBid.getBidAmount();
            autoBidAmount = loserCeiling.add(auction.getBidIncrement());
            if (autoBidAmount.compareTo(topAutoBid.getMaxAutoBid()) > 0) {
                autoBidAmount = topAutoBid.getMaxAutoBid();
            }
            winner = topAutoBid.getBidder();
            winnerMaxAutoBid = topAutoBid.getMaxAutoBid();
        }

        if (autoBidAmount.compareTo(auction.getCurrentPrice()) <= 0) return;

        newBid.setIsWinning(false);
        bidRepository.save(newBid);

        AuctionBid auto = new AuctionBid();
        auto.setAuction(auction);
        auto.setBidder(winner);
        auto.setBidAmount(autoBidAmount);
        auto.setIsAutoBid(true);
        auto.setMaxAutoBid(winnerMaxAutoBid);
        auto.setIsWinning(true);
        bidRepository.save(auto);

        auction.setCurrentPrice(autoBidAmount);
        auction.setTotalBids(auction.getTotalBids() + 1);
        auction.setWinner(winner);
        auction.setWinningBid(auto);
        auctionRepository.save(auction);

        webSocketService.sendNewBid(auction.getId(), winner.getUsername(), autoBidAmount, true);
        webSocketService.sendUpdate(auction);
        sendAuctionUpdate(auction);
    }

    private void sendAuctionUpdate(Auction auction) {
        webSocketService.sendUpdate(auction);
    }
}