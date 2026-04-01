package com.example.carnest.Enum;

public enum NotificationType {
    // Đơn hàng
    ORDER_PLACED,
    ORDER_CONFIRMED,
    ORDER_SHIPPED,
    ORDER_DELIVERED,
    ORDER_COMPLETED,
    ORDER_CANCELLED,

    // Đấu giá
    BID_PLACED,
    BID_OUTBID,
    AUCTION_WON,
    AUCTION_LOST,
    AUCTION_ENDING_SOON,
    AUCTION_STARTED,

    // Đề xuất giá
    OFFER_RECEIVED,
    OFFER_ACCEPTED,
    OFFER_REJECTED,
    OFFER_COUNTERED,
    OFFER_EXPIRED,

    // Trade / Đổi xe
    TRADE_PROPOSED,
    TRADE_ACCEPTED,
    TRADE_REJECTED,
    TRADE_COMPLETED,

    // Want list
    WANT_LIST_MATCH,

    // Social
    NEW_FOLLOWER,
    NEW_REVIEW,
    SHOP_NEW_PRODUCT,

    // Gamification
    BADGE_EARNED,
    RANK_UP,

    // Sản phẩm
    PRODUCT_PRICE_DROP,
    PRODUCT_SOLD,

    // Hệ thống
    SYSTEM_ANNOUNCEMENT,
    ACCOUNT_VERIFIED
}
