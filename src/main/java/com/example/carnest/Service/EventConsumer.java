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
    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private UserRepository userRepository;

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
}