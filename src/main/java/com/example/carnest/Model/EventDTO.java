package com.example.carnest.Model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class EventDTO {

    // ===== NOTIFICATION EVENT =====
    public static class NotificationEvent implements Serializable {
        private Long userId;
        private String type;
        private String title;
        private String content;
        private String referenceType;
        private Long referenceId;
        private String email; // cho email queue

        public NotificationEvent() {}
        public NotificationEvent(Long userId, String type, String title, String content,
                                 String referenceType, Long referenceId) {
            this.userId = userId; this.type = type; this.title = title;
            this.content = content; this.referenceType = referenceType; this.referenceId = referenceId;
        }

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getReferenceType() { return referenceType; }
        public void setReferenceType(String referenceType) { this.referenceType = referenceType; }
        public Long getReferenceId() { return referenceId; }
        public void setReferenceId(Long referenceId) { this.referenceId = referenceId; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    // ===== ORDER EVENT =====
    public static class OrderEvent implements Serializable {
        private Long orderId;
        private String orderCode;
        private Long buyerId;
        private Long sellerId;
        private String buyerUsername;
        private String sellerUsername;
        private BigDecimal totalAmount;
        private String status;
        private String action; // CREATED, PAID, CONFIRMED, SHIPPED, DELIVERED, COMPLETED, CANCELLED

        public OrderEvent() {}

        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }
        public String getOrderCode() { return orderCode; }
        public void setOrderCode(String orderCode) { this.orderCode = orderCode; }
        public Long getBuyerId() { return buyerId; }
        public void setBuyerId(Long buyerId) { this.buyerId = buyerId; }
        public Long getSellerId() { return sellerId; }
        public void setSellerId(Long sellerId) { this.sellerId = sellerId; }
        public String getBuyerUsername() { return buyerUsername; }
        public void setBuyerUsername(String buyerUsername) { this.buyerUsername = buyerUsername; }
        public String getSellerUsername() { return sellerUsername; }
        public void setSellerUsername(String sellerUsername) { this.sellerUsername = sellerUsername; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
    }

    // ===== ORDER EXPIRE EVENT =====
    public static class OrderExpireEvent implements Serializable {
        private Long orderId;

        public OrderExpireEvent() {}
        public OrderExpireEvent(Long orderId) { this.orderId = orderId; }
        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }
    }

    // ===== AUCTION BID EVENT =====
    public static class AuctionBidEvent implements Serializable {
        private Long auctionId;
        private Long bidderId;
        private String bidderUsername;
        private BigDecimal bidAmount;
        private boolean isAutoBid;
        private Long previousWinnerId; // người bị outbid

        public AuctionBidEvent() {}

        public Long getAuctionId() { return auctionId; }
        public void setAuctionId(Long auctionId) { this.auctionId = auctionId; }
        public Long getBidderId() { return bidderId; }
        public void setBidderId(Long bidderId) { this.bidderId = bidderId; }
        public String getBidderUsername() { return bidderUsername; }
        public void setBidderUsername(String bidderUsername) { this.bidderUsername = bidderUsername; }
        public BigDecimal getBidAmount() { return bidAmount; }
        public void setBidAmount(BigDecimal bidAmount) { this.bidAmount = bidAmount; }
        public boolean isAutoBid() { return isAutoBid; }
        public void setAutoBid(boolean autoBid) { isAutoBid = autoBid; }
        public Long getPreviousWinnerId() { return previousWinnerId; }
        public void setPreviousWinnerId(Long previousWinnerId) { this.previousWinnerId = previousWinnerId; }
    }

    // ===== OFFER EVENT =====
    public static class OfferEvent implements Serializable {
        private Long offerId;
        private Long buyerId;
        private Long sellerId;
        private String buyerUsername;
        private String sellerUsername;
        private String productName;
        private BigDecimal offerPrice;
        private BigDecimal counterPrice;
        private String action; // RECEIVED, ACCEPTED, REJECTED, COUNTERED, COUNTER_ACCEPTED, CANCELLED

        public OfferEvent() {}

        public Long getOfferId() { return offerId; }
        public void setOfferId(Long offerId) { this.offerId = offerId; }
        public Long getBuyerId() { return buyerId; }
        public void setBuyerId(Long buyerId) { this.buyerId = buyerId; }
        public Long getSellerId() { return sellerId; }
        public void setSellerId(Long sellerId) { this.sellerId = sellerId; }
        public String getBuyerUsername() { return buyerUsername; }
        public void setBuyerUsername(String buyerUsername) { this.buyerUsername = buyerUsername; }
        public String getSellerUsername() { return sellerUsername; }
        public void setSellerUsername(String sellerUsername) { this.sellerUsername = sellerUsername; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public BigDecimal getOfferPrice() { return offerPrice; }
        public void setOfferPrice(BigDecimal offerPrice) { this.offerPrice = offerPrice; }
        public BigDecimal getCounterPrice() { return counterPrice; }
        public void setCounterPrice(BigDecimal counterPrice) { this.counterPrice = counterPrice; }
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
    }

    // ===== TRADE EVENT =====
    public static class TradeEvent implements Serializable {
        private Long tradeId;
        private Long offererId;
        private Long receiverId;
        private String offererUsername;
        private String receiverUsername;
        private String targetProductName;
        private String action; // PROPOSED, ACCEPTED, REJECTED, CANCELLED

        public TradeEvent() {}

        public Long getTradeId() { return tradeId; }
        public void setTradeId(Long tradeId) { this.tradeId = tradeId; }
        public Long getOffererId() { return offererId; }
        public void setOffererId(Long offererId) { this.offererId = offererId; }
        public Long getReceiverId() { return receiverId; }
        public void setReceiverId(Long receiverId) { this.receiverId = receiverId; }
        public String getOffererUsername() { return offererUsername; }
        public void setOffererUsername(String offererUsername) { this.offererUsername = offererUsername; }
        public String getReceiverUsername() { return receiverUsername; }
        public void setReceiverUsername(String receiverUsername) { this.receiverUsername = receiverUsername; }
        public String getTargetProductName() { return targetProductName; }
        public void setTargetProductName(String targetProductName) { this.targetProductName = targetProductName; }
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
    }

    // ===== AUCTION CLOSE EVENT =====
    public static class AuctionCloseEvent implements Serializable {
        private Long auctionId;

        public AuctionCloseEvent() {}
        public AuctionCloseEvent(Long auctionId) { this.auctionId = auctionId; }
        public Long getAuctionId() { return auctionId; }
        public void setAuctionId(Long auctionId) { this.auctionId = auctionId; }
    }

    // ===== STATS UPDATE EVENT =====
    public static class StatsEvent implements Serializable {
        private Long userId;
        private String field; // totalBought, totalSold, ratingAvg
        private int increment;

        public StatsEvent() {}
        public StatsEvent(Long userId, String field, int increment) {
            this.userId = userId; this.field = field; this.increment = increment;
        }

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getField() { return field; }
        public void setField(String field) { this.field = field; }
        public int getIncrement() { return increment; }
        public void setIncrement(int increment) { this.increment = increment; }
    }
}