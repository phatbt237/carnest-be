package com.example.carnest.Service;

import com.example.carnest.Config.RabbitMQConfig;
import com.example.carnest.Entity.*;
import com.example.carnest.Enum.*;
import com.example.carnest.Model.EventDTO;
import com.example.carnest.Repository.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class EventConsumer {

    @Autowired private NotificationService notificationService;
    @Autowired private AuctionWebSocketService auctionWebSocketService;
    @Autowired private AuctionService auctionService;
    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private WantListRepository wantListRepository;

    // ===== NOTIFICATION PUSH — gửi WebSocket + lưu DB =====
    @RabbitListener(queues = RabbitMQConfig.QUEUE_NOTIFICATION_PUSH)
    public void handleNotificationPush(EventDTO.NotificationEvent event) {
        try {
            notificationService.send(
                    event.getUserId(),
                    NotificationType.valueOf(event.getType()),
                    event.getTitle(),
                    event.getContent(),
                    event.getReferenceType(),
                    event.getReferenceId()
            );
            System.out.println("[Consumer] Notification sent to user " + event.getUserId() + ": " + event.getTitle());
        } catch (Exception e) {
            System.err.println("[Consumer] Notification error: " + e.getMessage());
        }
    }

    // ===== EMAIL — gửi email (placeholder, tích hợp SMTP sau) =====
    @RabbitListener(queues = RabbitMQConfig.QUEUE_NOTIFICATION_EMAIL)
    public void handleEmailNotification(EventDTO.NotificationEvent event) {
        try {
            // TODO: tích hợp JavaMailSender
            System.out.println("[Consumer] Email to " + event.getEmail() + ": " + event.getTitle());
        } catch (Exception e) {
            System.err.println("[Consumer] Email error: " + e.getMessage());
        }
    }

    // ===== ORDER CREATED — notify seller =====
    @RabbitListener(queues = RabbitMQConfig.QUEUE_ORDER_CREATED)
    public void handleOrderCreated(EventDTO.OrderEvent event) {
        try {
            notificationService.send(event.getSellerId(), NotificationType.ORDER_PLACED,
                    "Đơn hàng mới #" + event.getOrderCode(),
                    event.getBuyerUsername() + " vừa đặt đơn " + event.getTotalAmount() + " VNĐ",
                    "ORDER", event.getOrderId());
            System.out.println("[Consumer] Order created event: " + event.getOrderCode());
        } catch (Exception e) {
            System.err.println("[Consumer] Order created error: " + e.getMessage());
        }
    }

    // ===== ORDER COMPLETED — notify seller =====
    @RabbitListener(queues = RabbitMQConfig.QUEUE_ORDER_COMPLETED)
    public void handleOrderCompleted(EventDTO.OrderEvent event) {
        try {
            notificationService.send(event.getSellerId(), NotificationType.ESCROW_RELEASED,
                    "Đã nhận tiền đơn #" + event.getOrderCode(),
                    "Đơn hàng hoàn thành, " + event.getTotalAmount() + " VNĐ đã chuyển vào ví",
                    "ORDER", event.getOrderId());
            System.out.println("[Consumer] Order completed: " + event.getOrderCode());
        } catch (Exception e) {
            System.err.println("[Consumer] Order completed error: " + e.getMessage());
        }
    }

    // ===== ORDER CANCELLED — notify cả 2 =====
    @RabbitListener(queues = RabbitMQConfig.QUEUE_ORDER_CANCELLED)
    public void handleOrderCancelled(EventDTO.OrderEvent event) {
        try {
            notificationService.send(event.getBuyerId(), NotificationType.ORDER_CANCELLED,
                    "Đơn #" + event.getOrderCode() + " đã bị hủy",
                    "Đơn hàng đã bị hủy, tiền sẽ được hoàn lại",
                    "ORDER", event.getOrderId());
            notificationService.send(event.getSellerId(), NotificationType.ORDER_CANCELLED,
                    "Đơn #" + event.getOrderCode() + " đã bị hủy",
                    "Đơn hàng đã bị hủy bởi " + event.getBuyerUsername(),
                    "ORDER", event.getOrderId());
        } catch (Exception e) {
            System.err.println("[Consumer] Order cancelled error: " + e.getMessage());
        }
    }

    // ===== ORDER EXPIRE — hủy đơn chưa thanh toán (delayed message) =====
    @RabbitListener(queues = RabbitMQConfig.QUEUE_ORDER_EXPIRE)
    @Transactional
    public void handleOrderExpire(EventDTO.OrderExpireEvent event) {
        try {
            Order order = orderRepository.findById(event.getOrderId()).orElse(null);
            if (order == null || order.getStatus() != OrderStatus.PENDING_PAYMENT) return;

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

            System.out.println("[Consumer] Order expired: " + order.getOrderCode());
        } catch (Exception e) {
            System.err.println("[Consumer] Order expire error: " + e.getMessage());
        }
    }

    // ===== AUCTION CLOSE — đóng phiên khi hết giờ (delayed message) =====
    @RabbitListener(queues = RabbitMQConfig.QUEUE_AUCTION_CLOSE)
    public void handleAuctionClose(EventDTO.AuctionCloseEvent event) {
        auctionService.closeExpiredAuction(event.getAuctionId());
        System.out.println("[Consumer] Auction close processed: #" + event.getAuctionId());
    }

    // ===== AUCTION CLOSE DLQ — retry lần cuối sau 1 phút, nếu vẫn fail thì bỏ =====
    @RabbitListener(queues = RabbitMQConfig.QUEUE_AUCTION_CLOSE_DLQ)
    public void handleAuctionCloseDlq(EventDTO.AuctionCloseEvent event) {
        System.err.println("[DLQ] Retry auction close lần cuối: #" + event.getAuctionId());
        try {
            auctionService.closeExpiredAuction(event.getAuctionId());
            System.out.println("[DLQ] Thành công auction #" + event.getAuctionId());
        } catch (Exception e) {
            System.err.println("[DLQ] Thất bại hoàn toàn auction #" + event.getAuctionId() + ": " + e.getMessage());
            // Scheduler 10 phút sẽ xử lý tiếp
        }
    }

    // ===== AUCTION BID — notify outbid user + WebSocket =====
    @RabbitListener(queues = RabbitMQConfig.QUEUE_AUCTION_BID)
    public void handleAuctionBid(EventDTO.AuctionBidEvent event) {
        try {
            // Notify người bị outbid
            if (event.getPreviousWinnerId() != null && !event.getPreviousWinnerId().equals(event.getBidderId())) {
                notificationService.send(event.getPreviousWinnerId(), NotificationType.BID_OUTBID,
                        "Bạn đã bị outbid!",
                        event.getBidderUsername() + " vừa bid " + event.getBidAmount() + " VNĐ, vượt qua bạn",
                        "AUCTION", event.getAuctionId());
            }
            System.out.println("[Consumer] Auction bid: " + event.getBidderUsername() + " bid " + event.getBidAmount());
        } catch (Exception e) {
            System.err.println("[Consumer] Auction bid error: " + e.getMessage());
        }
    }

    // ===== OFFER NOTIFICATION =====
    @RabbitListener(queues = RabbitMQConfig.QUEUE_OFFER_NOTIFICATION)
    public void handleOfferNotification(EventDTO.OfferEvent event) {
        try {
            switch (event.getAction()) {
                case "RECEIVED":
                    notificationService.send(event.getSellerId(), NotificationType.OFFER_RECEIVED,
                            "Đề xuất giá mới cho \"" + event.getProductName() + "\"",
                            event.getBuyerUsername() + " đề xuất " + event.getOfferPrice() + " VNĐ",
                            "OFFER", event.getOfferId());
                    break;
                case "ACCEPTED":
                    notificationService.send(event.getBuyerId(), NotificationType.OFFER_ACCEPTED,
                            "Đề xuất giá được chấp nhận!",
                            event.getSellerUsername() + " đã chấp nhận giá " + event.getOfferPrice() + " VNĐ cho \"" + event.getProductName() + "\"",
                            "OFFER", event.getOfferId());
                    break;
                case "REJECTED":
                    notificationService.send(event.getBuyerId(), NotificationType.OFFER_REJECTED,
                            "Đề xuất giá bị từ chối",
                            event.getSellerUsername() + " đã từ chối đề xuất giá của bạn cho \"" + event.getProductName() + "\"",
                            "OFFER", event.getOfferId());
                    break;
                case "COUNTERED":
                    notificationService.send(event.getBuyerId(), NotificationType.OFFER_COUNTERED,
                            "Người bán đề xuất giá lại",
                            event.getSellerUsername() + " đề xuất giá " + event.getCounterPrice() + " VNĐ cho \"" + event.getProductName() + "\"",
                            "OFFER", event.getOfferId());
                    break;
                case "COUNTER_ACCEPTED":
                    notificationService.send(event.getSellerId(), NotificationType.OFFER_ACCEPTED,
                            "Người mua đồng ý giá counter!",
                            event.getBuyerUsername() + " đã chấp nhận giá " + event.getOfferPrice() + " VNĐ cho \"" + event.getProductName() + "\"",
                            "OFFER", event.getOfferId());
                    break;
                case "CANCELLED":
                    notificationService.send(event.getSellerId(), NotificationType.OFFER_CANCELLED,
                            "Người mua đã hủy đề xuất giá",
                            event.getBuyerUsername() + " đã hủy đề xuất giá cho \"" + event.getProductName() + "\"",
                            "OFFER", event.getOfferId());
                    break;
            }
            System.out.println("[Consumer] Offer event: " + event.getAction() + " offerId=" + event.getOfferId());
        } catch (Exception e) {
            System.err.println("[Consumer] Offer notification error: " + e.getMessage());
        }
    }

    // ===== TRADE NOTIFICATION =====
    @RabbitListener(queues = RabbitMQConfig.QUEUE_TRADE_NOTIFICATION)
    public void handleTradeNotification(EventDTO.TradeEvent event) {
        try {
            switch (event.getAction()) {
                case "PROPOSED":
                    notificationService.send(event.getReceiverId(), NotificationType.TRADE_PROPOSED,
                            "Đề xuất đổi xe mới",
                            event.getOffererUsername() + " muốn đổi xe lấy \"" + event.getTargetProductName() + "\"",
                            "TRADE", event.getTradeId());
                    break;
                case "ACCEPTED":
                    notificationService.send(event.getOffererId(), NotificationType.TRADE_ACCEPTED,
                            "Đề xuất đổi xe được chấp nhận!",
                            event.getReceiverUsername() + " đã chấp nhận đề xuất đổi xe của bạn cho \"" + event.getTargetProductName() + "\"",
                            "TRADE", event.getTradeId());
                    break;
                case "REJECTED":
                    notificationService.send(event.getOffererId(), NotificationType.TRADE_REJECTED,
                            "Đề xuất đổi xe bị từ chối",
                            event.getReceiverUsername() + " đã từ chối đề xuất đổi xe của bạn cho \"" + event.getTargetProductName() + "\"",
                            "TRADE", event.getTradeId());
                    break;
                case "CANCELLED":
                    notificationService.send(event.getReceiverId(), NotificationType.TRADE_CANCELLED,
                            "Đề xuất đổi xe đã bị hủy",
                            event.getOffererUsername() + " đã hủy đề xuất đổi xe cho \"" + event.getTargetProductName() + "\"",
                            "TRADE", event.getTradeId());
                    break;
            }
            System.out.println("[Consumer] Trade event: " + event.getAction() + " tradeId=" + event.getTradeId());
        } catch (Exception e) {
            System.err.println("[Consumer] Trade notification error: " + e.getMessage());
        }
    }

    // ===== STATS UPDATE — cập nhật thống kê async =====
    @RabbitListener(queues = RabbitMQConfig.QUEUE_STATS_UPDATE)
    @Transactional
    public void handleStatsUpdate(EventDTO.StatsEvent event) {
        try {
            User user = userRepository.findById(event.getUserId()).orElse(null);
            if (user == null) return;

            switch (event.getField()) {
                case "totalBought":
                    user.setTotalBought(user.getTotalBought() + event.getIncrement());
                    break;
                case "totalSold":
                    user.setTotalSold(user.getTotalSold() + event.getIncrement());
                    break;
            }
            userRepository.save(user);
            System.out.println("[Consumer] Stats updated: user " + user.getUsername() + " " + event.getField() + " +" + event.getIncrement());
        } catch (Exception e) {
            System.err.println("[Consumer] Stats update error: " + e.getMessage());
        }
    }

    // ===== WANTLIST CONTACT — cộng dồn số người đã liên hệ (async) =====
    @RabbitListener(queues = RabbitMQConfig.QUEUE_WANTLIST_CONTACT)
    @Transactional
    public void handleWantListContact(EventDTO.WantListContactEvent event) {
        try {
            wantListRepository.incrementContactCount(event.getWantListId());
            System.out.println("[Consumer] WantList contactCount +1: id=" + event.getWantListId());
        } catch (Exception e) {
            System.err.println("[Consumer] WantList contact update error: " + e.getMessage());
        }
    }
}