package com.example.carnest.Service;

import com.example.carnest.Entity.*;
import com.example.carnest.Enum.*;
import com.example.carnest.Exception.BadRequestException;
import com.example.carnest.Exception.ResourceNotFoundException;
import com.example.carnest.Model.PriceOfferDTO;
import com.example.carnest.Model.ShopDTO;
import com.example.carnest.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.carnest.Entity.Order;
import com.example.carnest.Entity.OrderItem;
import com.example.carnest.Entity.OrderStatusHistory;
import com.example.carnest.Repository.OrderRepository;
import com.example.carnest.Repository.OrderItemRepository;
import com.example.carnest.Repository.OrderStatusHistoryRepository;
import com.example.carnest.Repository.ShopRepository;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PriceOfferService {

    private final PriceOfferRepository offerRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusHistoryRepository statusHistoryRepository;
    private final ShopRepository shopRepository;

    private static final int OFFER_EXPIRE_HOURS = 48;
    private static final int MAX_SIZE = 50;

    @Autowired
    public PriceOfferService(PriceOfferRepository offerRepository, ProductRepository productRepository,
                             ProductImageRepository productImageRepository, UserRepository userRepository,
                             OrderRepository orderRepository, OrderItemRepository orderItemRepository,
                             OrderStatusHistoryRepository statusHistoryRepository, ShopRepository shopRepository) {
        this.offerRepository = offerRepository;
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.shopRepository = shopRepository;
    }

    // ===== GỬI OFFER =====
    @Transactional
    public PriceOfferDTO.OfferResponse createOffer(Long buyerId, PriceOfferDTO.CreateRequest request) {
        Product product = productRepository.findByIdFull(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));

        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new BadRequestException("Sản phẩm không còn bán");
        }
        if (!product.getAllowOffer()) {
            throw new BadRequestException("Sản phẩm này không cho phép đề xuất giá");
        }
        if (product.getShop().getUser().getId().equals(buyerId)) {
            throw new BadRequestException("Bạn không thể offer sản phẩm của chính mình");
        }
        if (offerRepository.existsByBuyerIdAndProductIdAndStatus(buyerId, request.getProductId(), OfferStatus.PENDING)) {
            throw new BadRequestException("Bạn đã có offer đang chờ cho sản phẩm này");
        }
        if (product.getMinOfferPrice() != null && request.getOfferPrice().compareTo(product.getMinOfferPrice()) < 0) {
            throw new BadRequestException("Giá đề xuất tối thiểu là " + product.getMinOfferPrice() + " VNĐ");
        }

        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", buyerId));

        PriceOffer offer = new PriceOffer();
        offer.setProduct(product);
        offer.setBuyer(buyer);
        offer.setSeller(product.getShop().getUser());
        offer.setOfferPrice(request.getOfferPrice());
        offer.setMessage(request.getMessage());
        offer.setStatus(OfferStatus.PENDING);
        offer.setExpiresAt(LocalDateTime.now().plusHours(OFFER_EXPIRE_HOURS));

        offer = offerRepository.save(offer);
        return toOfferResponse(offer);
    }

    // ===== SELLER CHẤP NHẬN =====
    @Transactional
    public PriceOfferDTO.OfferResponse accept(Long sellerId, Long offerId) {
        PriceOffer offer = getOfferForSeller(sellerId, offerId);
        validatePending(offer);

        offer.setStatus(OfferStatus.ACCEPTED);
        offer.setRespondedAt(LocalDateTime.now());
        offerRepository.save(offer);

        // Tự động tạo order với giá offer
        createOrderFromOffer(offer);

        return toOfferResponse(offer);
    }

    // ===== SELLER TỪ CHỐI =====
    @Transactional
    public PriceOfferDTO.OfferResponse reject(Long sellerId, Long offerId) {
        PriceOffer offer = getOfferForSeller(sellerId, offerId);
        validatePending(offer);

        offer.setStatus(OfferStatus.REJECTED);
        offer.setRespondedAt(LocalDateTime.now());
        offerRepository.save(offer);
        return toOfferResponse(offer);
    }

    // ===== SELLER COUNTER =====
    @Transactional
    public PriceOfferDTO.OfferResponse counter(Long sellerId, Long offerId, PriceOfferDTO.CounterRequest request) {
        PriceOffer offer = getOfferForSeller(sellerId, offerId);
        validatePending(offer);

        offer.setStatus(OfferStatus.COUNTERED);
        offer.setCounterPrice(request.getCounterPrice());
        offer.setRespondedAt(LocalDateTime.now());
        offer.setExpiresAt(LocalDateTime.now().plusHours(OFFER_EXPIRE_HOURS)); // reset deadline
        offerRepository.save(offer);
        return toOfferResponse(offer);
    }

    // ===== BUYER CHẤP NHẬN COUNTER =====
    @Transactional
    public PriceOfferDTO.OfferResponse acceptCounter(Long buyerId, Long offerId) {
        PriceOffer offer = offerRepository.findByIdFull(offerId)
                .orElseThrow(() -> new ResourceNotFoundException("Offer", "id", offerId));
        if (!offer.getBuyer().getId().equals(buyerId)) {
            throw new BadRequestException("Bạn không có quyền thao tác offer này");
        }
        if (offer.getStatus() != OfferStatus.COUNTERED) {
            throw new BadRequestException("Offer không ở trạng thái counter");
        }

        offer.setStatus(OfferStatus.ACCEPTED);
        offer.setOfferPrice(offer.getCounterPrice());
        offer.setRespondedAt(LocalDateTime.now());
        offerRepository.save(offer);

        // Tự động tạo order với giá counter
        createOrderFromOffer(offer);

        return toOfferResponse(offer);
    }

    // ===== BUYER HỦY =====
    @Transactional
    public PriceOfferDTO.OfferResponse cancelOffer(Long buyerId, Long offerId) {
        PriceOffer offer = offerRepository.findByIdFull(offerId)
                .orElseThrow(() -> new ResourceNotFoundException("Offer", "id", offerId));
        if (!offer.getBuyer().getId().equals(buyerId)) {
            throw new BadRequestException("Bạn không có quyền hủy offer này");
        }
        if (offer.getStatus() != OfferStatus.PENDING && offer.getStatus() != OfferStatus.COUNTERED) {
            throw new BadRequestException("Không thể hủy offer ở trạng thái " + offer.getStatus());
        }

        offer.setStatus(OfferStatus.CANCELLED);
        offerRepository.save(offer);
        return toOfferResponse(offer);
    }

    // ===== DANH SÁCH OFFER CỦA BUYER =====
    public ShopDTO.CursorPage<PriceOfferDTO.OfferResponse> getMyOffers(Long buyerId, String cursor, int size) {
        size = Math.min(Math.max(size, 1), MAX_SIZE);
        Long cursorId = (cursor != null && !cursor.isEmpty()) ? Long.parseLong(cursor) : null;

        List<PriceOffer> offers = offerRepository.findByBuyerId(buyerId, cursorId, size + 1);
        boolean hasMore = offers.size() > size;
        if (hasMore) offers = offers.subList(0, size);

        List<PriceOfferDTO.OfferResponse> items = offers.stream()
                .map(this::toOfferResponse).collect(Collectors.toList());
        String nextCursor = hasMore && !offers.isEmpty()
                ? String.valueOf(offers.get(offers.size() - 1).getId()) : null;

        return new ShopDTO.CursorPage<>(items, nextCursor, hasMore, items.size(), offerRepository.countByBuyerId(buyerId));
    }

    // ===== DANH SÁCH OFFER CHO SELLER =====
    public ShopDTO.CursorPage<PriceOfferDTO.OfferResponse> getShopOffers(Long sellerId, String cursor, int size) {
        size = Math.min(Math.max(size, 1), MAX_SIZE);
        Long cursorId = (cursor != null && !cursor.isEmpty()) ? Long.parseLong(cursor) : null;

        List<PriceOffer> offers = offerRepository.findBySellerId(sellerId, cursorId, size + 1);
        boolean hasMore = offers.size() > size;
        if (hasMore) offers = offers.subList(0, size);

        List<PriceOfferDTO.OfferResponse> items = offers.stream()
                .map(this::toOfferResponse).collect(Collectors.toList());
        String nextCursor = hasMore && !offers.isEmpty()
                ? String.valueOf(offers.get(offers.size() - 1).getId()) : null;

        return new ShopDTO.CursorPage<>(items, nextCursor, hasMore, items.size(), offerRepository.countBySellerId(sellerId));
    }

    // ===== HELPERS =====
    private PriceOffer getOfferForSeller(Long sellerId, Long offerId) {
        PriceOffer offer = offerRepository.findByIdFull(offerId)
                .orElseThrow(() -> new ResourceNotFoundException("Offer", "id", offerId));
        if (!offer.getSeller().getId().equals(sellerId)) {
            throw new BadRequestException("Bạn không có quyền thao tác offer này");
        }
        return offer;
    }

    private void validatePending(PriceOffer offer) {
        if (offer.getStatus() != OfferStatus.PENDING) {
            throw new BadRequestException("Offer không ở trạng thái chờ phản hồi");
        }
        if (offer.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Offer đã hết hạn");
        }
    }

    private PriceOfferDTO.OfferResponse toOfferResponse(PriceOffer o) {
        PriceOfferDTO.OfferResponse r = new PriceOfferDTO.OfferResponse();
        r.setId(o.getId());
        r.setProductId(o.getProduct().getId());
        r.setProductName(o.getProduct().getName());
        r.setProductPrice(o.getProduct().getPrice());
        r.setOfferPrice(o.getOfferPrice());
        r.setCounterPrice(o.getCounterPrice());
        r.setMessage(o.getMessage());
        r.setStatus(o.getStatus().name());
        r.setBuyerUsername(o.getBuyer().getUsername());
        r.setSellerUsername(o.getSeller().getUsername());
        r.setExpiresAt(o.getExpiresAt());
        r.setRespondedAt(o.getRespondedAt());
        r.setCreatedAt(o.getCreatedAt());

        List<ProductImage> imgs = productImageRepository.findPrimaryByProductIds(List.of(o.getProduct().getId()));
        if (!imgs.isEmpty()) r.setProductImage(imgs.get(0).getImageUrl());

        return r;
    }

    private void createOrderFromOffer(PriceOffer offer) {
        Product product = productRepository.findByIdForUpdate(offer.getProduct().getId())
                .orElseThrow(() -> new BadRequestException("Sản phẩm không tồn tại"));

        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new BadRequestException("Sản phẩm không còn bán");
        }
        if (product.getQuantity() < 1) {
            throw new BadRequestException("Sản phẩm đã hết hàng");
        }

        // Trừ quantity
        product.setQuantity(product.getQuantity() - 1);
        if (product.getQuantity() == 0) {
            product.setStatus(ProductStatus.SOLD);
        }
        productRepository.save(product);

        // Giá cuối cùng = offerPrice (đã được set đúng ở accept/acceptCounter)
        BigDecimal finalPrice = offer.getOfferPrice();

        // Tạo order — PENDING_PAYMENT (buyer cần thanh toán trong 5 phút)
        Order order = new Order();
        order.setOrderCode(generateOrderCode());
        order.setBuyer(offer.getBuyer());
        order.setShop(product.getShop());
        order.setShippingName("");  // buyer sẽ cập nhật khi pay
        order.setShippingPhone("");
        order.setShippingAddress("");
        order.setSubtotal(finalPrice);
        order.setShippingFee(BigDecimal.ZERO);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setTotalAmount(finalPrice);
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setEscrowStatus(EscrowStatus.NONE);
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        order.setPaymentDeadline(LocalDateTime.now().plusMinutes(5));
        order.setBuyerNote("Đơn từ offer #" + offer.getId());
        order = orderRepository.save(order);

        // Tạo order item
        List<ProductImage> imgs = productImageRepository.findPrimaryByProductIds(List.of(product.getId()));
        String imgUrl = imgs.isEmpty() ? null : imgs.get(0).getImageUrl();

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProduct(product);
        item.setProductName(product.getName());
        item.setProductImage(imgUrl);
        item.setPrice(finalPrice);
        item.setQuantity(1);
        orderItemRepository.save(item);

        // Ghi lịch sử
        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrder(order);
        history.setToStatus(OrderStatus.PENDING_PAYMENT.name());
        history.setNote("Đơn tạo từ offer thỏa thuận giá " + finalPrice + " VNĐ — chờ thanh toán 5 phút");
        statusHistoryRepository.save(history);
    }

    private String generateOrderCode() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = String.format("%04d", new Random().nextInt(10000));
        return "ORD-" + date + "-" + random;
    }
}