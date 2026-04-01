package com.example.carnest.Service;

import com.example.carnest.Entity.*;
import com.example.carnest.Enum.*;
import com.example.carnest.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class OrderScheduler {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private AuctionBidRepository auctionBidRepository;

    @Autowired
    private PriceOfferRepository priceOfferRepository;

    @Autowired
    private OrderStatusHistoryRepository statusHistoryRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuctionWebSocketService auctionWebSocketService;

    @Scheduled(fixedRate = 30000)
    @Transactional
    public void expirePendingPaymentOrders() {
        List<Order> expired = orderRepository.findExpiredPendingPayment(LocalDateTime.now());

        for (Order order : expired) {
            order.setStatus(OrderStatus.EXPIRED);
            orderRepository.save(order);

            List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
            for (OrderItem oi : items) {
                Product p = oi.getProduct();
                p.setQuantity(p.getQuantity() + oi.getQuantity());
                if (p.getStatus() == ProductStatus.SOLD) {
                    p.setStatus(ProductStatus.ACTIVE);
                }
                productRepository.save(p);
            }
        }

        if (!expired.isEmpty()) {
            System.out.println("[OrderScheduler] Hủy " + expired.size() + " đơn hết hạn thanh toán");
        }
    }

    // Chạy mỗi 30s — kích hoạt auction UPCOMING
    @Scheduled(fixedRate = 30000)
    @Transactional
    public void startUpcomingAuctions() {
        List<Auction> toStart = auctionRepository.findAuctionsToStart(LocalDateTime.now());
        for (Auction a : toStart) {
            a.setStatus(AuctionStatus.ACTIVE);
            auctionRepository.save(a);
        }
    }

    // Chạy mỗi 30s — kết thúc auction hết giờ
//    @Scheduled(fixedRate = 30000)
    @Transactional
    public void endExpiredAuctions() {
        List<Auction> expired = auctionRepository.findExpiredAuctions(LocalDateTime.now());
        for (Auction a : expired) {
            if (a.getWinner() != null) {
                // Có người thắng
                if (a.getReservePrice() != null && a.getCurrentPrice().compareTo(a.getReservePrice()) < 0) {
                    // Không đạt reserve price
                    a.setStatus(AuctionStatus.NO_SALE);
                    a.getProduct().setStatus(ProductStatus.ACTIVE);
                    productRepository.save(a.getProduct());
                } else {
                    a.setStatus(AuctionStatus.ENDED);
                    // Tạo order cho winner
                    createOrderFromAuction(a);
                }
            } else {
                // Không ai bid
                a.setStatus(AuctionStatus.NO_SALE);
                a.getProduct().setStatus(ProductStatus.ACTIVE);
                productRepository.save(a.getProduct());
            }
            auctionRepository.save(a);
            auctionWebSocketService.sendAuctionEnded(a);
        }
        if (!expired.isEmpty()) {
            System.out.println("[Scheduler] Kết thúc " + expired.size() + " phiên đấu giá");
        }
    }

    // Chạy mỗi 5 phút — expire offer hết hạn
    @Scheduled(fixedRate = 300000)
    @Transactional
    public void expireOffers() {
        List<PriceOffer> expired = priceOfferRepository.findExpiredOffers(LocalDateTime.now());
        for (PriceOffer o : expired) {
            o.setStatus(OfferStatus.EXPIRED);
            priceOfferRepository.save(o);
        }
        if (!expired.isEmpty()) {
            System.out.println("[Scheduler] Expire " + expired.size() + " offers");
        }
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
        order.setPaymentDeadline(LocalDateTime.now().plusMinutes(30)); // auction cho 30 phút thanh toán
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
        statusHistoryRepository.save(history);

        System.out.println("[Scheduler] Tạo order " + order.getOrderCode() + " cho winner " + auction.getWinner().getUsername());
    }
}