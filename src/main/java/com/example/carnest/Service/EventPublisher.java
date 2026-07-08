package com.example.carnest.Service;

import com.example.carnest.Config.RabbitMQConfig;
import com.example.carnest.Model.EventDTO;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class EventPublisher {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    // ===== NOTIFICATION =====
    public void publishNotification(Long userId, String type, String title,
                                    String content, String refType, Long refId) {
        EventDTO.NotificationEvent event = new EventDTO.NotificationEvent(
                userId, type, title, content, refType, refId);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NOTIFICATION,
                RabbitMQConfig.RK_NOTIFICATION_PUSH, event);
    }

    public void publishEmailNotification(Long userId, String type, String title,
                                         String content, String email) {
        EventDTO.NotificationEvent event = new EventDTO.NotificationEvent(
                userId, type, title, content, null, null);
        event.setEmail(email);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NOTIFICATION,
                RabbitMQConfig.RK_NOTIFICATION_EMAIL, event);
    }

    // ===== ORDER =====
    public void publishOrderEvent(EventDTO.OrderEvent event) {
        String routingKey;
        switch (event.getAction()) {
            case "CREATED": routingKey = RabbitMQConfig.RK_ORDER_CREATED; break;
            case "COMPLETED": routingKey = RabbitMQConfig.RK_ORDER_COMPLETED; break;
            case "CANCELLED": routingKey = RabbitMQConfig.RK_ORDER_CANCELLED; break;
            default: routingKey = RabbitMQConfig.RK_ORDER_CREATED;
        }
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_ORDER, routingKey, event);
    }

    // ===== ORDER EXPIRE (delayed) =====
    public void publishOrderExpire(Long orderId, long delayMs) {
        EventDTO.OrderExpireEvent event = new EventDTO.OrderExpireEvent(orderId);
        MessagePostProcessor processor = message -> {
            message.getMessageProperties().setHeader("x-delay", delayMs);
            return message;
        };
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_DELAYED,
                RabbitMQConfig.RK_ORDER_EXPIRE, event, processor);
    }

    // ===== AUCTION CLOSE (delayed) =====
    public void publishAuctionClose(Long auctionId, LocalDateTime endTime) {
        EventDTO.AuctionCloseEvent event = new EventDTO.AuctionCloseEvent(auctionId);
        long delayMs = ChronoUnit.MILLIS.between(LocalDateTime.now(), endTime);
        if (delayMs < 0) delayMs = 0;
        final long finalDelayMs = delayMs;
        MessagePostProcessor processor = message -> {
            message.getMessageProperties().setHeader("x-delay", finalDelayMs);
            return message;
        };
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_DELAYED,
                RabbitMQConfig.RK_AUCTION_CLOSE, event, processor);
    }

    // ===== AUCTION =====
    public void publishAuctionBid(EventDTO.AuctionBidEvent event) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_AUCTION,
                RabbitMQConfig.RK_AUCTION_BID, event);
    }

    public void publishAuctionEnded(Long auctionId) {
        EventDTO.OrderExpireEvent event = new EventDTO.OrderExpireEvent(auctionId);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_AUCTION,
                RabbitMQConfig.RK_AUCTION_ENDED, event);
    }

    // ===== OFFER =====
    public void publishOfferEvent(EventDTO.OfferEvent event) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NOTIFICATION,
                RabbitMQConfig.RK_OFFER_NOTIFICATION, event);
    }

    // ===== TRADE =====
    public void publishTradeEvent(EventDTO.TradeEvent event) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NOTIFICATION,
                RabbitMQConfig.RK_TRADE_NOTIFICATION, event);
    }

    // ===== STATS =====
    public void publishStatsUpdate(Long userId, String field, int increment) {
        EventDTO.StatsEvent event = new EventDTO.StatsEvent(userId, field, increment);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_ORDER,
                RabbitMQConfig.RK_STATS_UPDATE, event);
    }

    // ===== WANTLIST CONTACT =====
    public void publishWantListContact(Long wantListId) {
        EventDTO.WantListContactEvent event = new EventDTO.WantListContactEvent(wantListId);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_ORDER,
                RabbitMQConfig.RK_WANTLIST_CONTACT, event);
    }
}