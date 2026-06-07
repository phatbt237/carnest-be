package com.example.carnest.Config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    // ===== EXCHANGES =====
    public static final String EXCHANGE_NOTIFICATION = "carnest.notification";
    public static final String EXCHANGE_ORDER = "carnest.order";
    public static final String EXCHANGE_AUCTION = "carnest.auction";
    public static final String EXCHANGE_DELAYED = "carnest.delayed";

    // ===== QUEUES =====
    public static final String QUEUE_NOTIFICATION_PUSH = "notification.push";
    public static final String QUEUE_NOTIFICATION_EMAIL = "notification.email";
    public static final String QUEUE_ORDER_CREATED = "order.created";
    public static final String QUEUE_ORDER_COMPLETED = "order.completed";
    public static final String QUEUE_ORDER_CANCELLED = "order.cancelled";
    public static final String QUEUE_ORDER_EXPIRE = "order.expire";
    public static final String QUEUE_AUCTION_BID = "auction.bid";
    public static final String QUEUE_AUCTION_ENDED = "auction.ended";
    public static final String QUEUE_AUCTION_CLOSE = "auction.close";
    public static final String QUEUE_AUCTION_CLOSE_DLQ = "auction.close.dlq";
    public static final String QUEUE_STATS_UPDATE = "stats.update";
    public static final String QUEUE_OFFER_NOTIFICATION = "offer.notification";
    public static final String QUEUE_TRADE_NOTIFICATION = "trade.notification";

    // ===== ROUTING KEYS =====
    public static final String RK_NOTIFICATION_PUSH = "notification.push";
    public static final String RK_NOTIFICATION_EMAIL = "notification.email";
    public static final String RK_ORDER_CREATED = "order.created";
    public static final String RK_ORDER_COMPLETED = "order.completed";
    public static final String RK_ORDER_CANCELLED = "order.cancelled";
    public static final String RK_ORDER_EXPIRE = "order.expire";
    public static final String RK_AUCTION_BID = "auction.bid";
    public static final String RK_AUCTION_ENDED = "auction.ended";
    public static final String RK_AUCTION_CLOSE = "auction.close";
    public static final String RK_STATS_UPDATE = "stats.update";
    public static final String RK_OFFER_NOTIFICATION = "offer.notification";
    public static final String RK_TRADE_NOTIFICATION = "trade.notification";

    // ===== JSON Converter =====
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    // ===== EXCHANGES =====
    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(EXCHANGE_NOTIFICATION);
    }

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(EXCHANGE_ORDER);
    }

    @Bean
    public TopicExchange auctionExchange() {
        return new TopicExchange(EXCHANGE_AUCTION);
    }

    // Delayed exchange — cho order expire chính xác
    @Bean
    public CustomExchange delayedExchange() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-delayed-type", "topic");
        return new CustomExchange(EXCHANGE_DELAYED, "x-delayed-message", true, false, args);
    }

    // ===== QUEUES =====
    @Bean public Queue notificationPushQueue() { return new Queue(QUEUE_NOTIFICATION_PUSH, true); }
    @Bean public Queue notificationEmailQueue() { return new Queue(QUEUE_NOTIFICATION_EMAIL, true); }
    @Bean public Queue orderCreatedQueue() { return new Queue(QUEUE_ORDER_CREATED, true); }
    @Bean public Queue orderCompletedQueue() { return new Queue(QUEUE_ORDER_COMPLETED, true); }
    @Bean public Queue orderCancelledQueue() { return new Queue(QUEUE_ORDER_CANCELLED, true); }
    @Bean public Queue orderExpireQueue() { return new Queue(QUEUE_ORDER_EXPIRE, true); }
    @Bean public Queue auctionBidQueue() { return new Queue(QUEUE_AUCTION_BID, true); }
    @Bean public Queue auctionEndedQueue() { return new Queue(QUEUE_AUCTION_ENDED, true); }
    @Bean public Queue auctionCloseQueue() {
        return QueueBuilder.durable(QUEUE_AUCTION_CLOSE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", QUEUE_AUCTION_CLOSE_DLQ)
                .build();
    }
    @Bean public Queue auctionCloseDlqQueue() { return new Queue(QUEUE_AUCTION_CLOSE_DLQ, true); }
    @Bean public Queue statsUpdateQueue() { return new Queue(QUEUE_STATS_UPDATE, true); }
    @Bean public Queue offerNotificationQueue() { return new Queue(QUEUE_OFFER_NOTIFICATION, true); }
    @Bean public Queue tradeNotificationQueue() { return new Queue(QUEUE_TRADE_NOTIFICATION, true); }

    // ===== BINDINGS =====
    @Bean public Binding bindNotificationPush() {
        return BindingBuilder.bind(notificationPushQueue()).to(notificationExchange()).with(RK_NOTIFICATION_PUSH);
    }
    @Bean public Binding bindNotificationEmail() {
        return BindingBuilder.bind(notificationEmailQueue()).to(notificationExchange()).with(RK_NOTIFICATION_EMAIL);
    }
    @Bean public Binding bindOrderCreated() {
        return BindingBuilder.bind(orderCreatedQueue()).to(orderExchange()).with(RK_ORDER_CREATED);
    }
    @Bean public Binding bindOrderCompleted() {
        return BindingBuilder.bind(orderCompletedQueue()).to(orderExchange()).with(RK_ORDER_COMPLETED);
    }
    @Bean public Binding bindOrderCancelled() {
        return BindingBuilder.bind(orderCancelledQueue()).to(orderExchange()).with(RK_ORDER_CANCELLED);
    }
    @Bean public Binding bindOrderExpire() {
        return BindingBuilder.bind(orderExpireQueue()).to(delayedExchange()).with(RK_ORDER_EXPIRE).noargs();
    }
    @Bean public Binding bindAuctionBid() {
        return BindingBuilder.bind(auctionBidQueue()).to(auctionExchange()).with(RK_AUCTION_BID);
    }
    @Bean public Binding bindAuctionEnded() {
        return BindingBuilder.bind(auctionEndedQueue()).to(auctionExchange()).with(RK_AUCTION_ENDED);
    }
    @Bean public Binding bindAuctionClose() {
        return BindingBuilder.bind(auctionCloseQueue()).to(delayedExchange()).with(RK_AUCTION_CLOSE).noargs();
    }
    @Bean public Binding bindStatsUpdate() {
        return BindingBuilder.bind(statsUpdateQueue()).to(orderExchange()).with(RK_STATS_UPDATE);
    }
    @Bean public Binding bindOfferNotification() {
        return BindingBuilder.bind(offerNotificationQueue()).to(notificationExchange()).with(RK_OFFER_NOTIFICATION);
    }
    @Bean public Binding bindTradeNotification() {
        return BindingBuilder.bind(tradeNotificationQueue()).to(notificationExchange()).with(RK_TRADE_NOTIFICATION);
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        admin.setAutoStartup(true);
        return admin;
    }
}