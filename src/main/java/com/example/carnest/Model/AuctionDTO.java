package com.example.carnest.Model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class AuctionDTO {

    // ===== TẠO AUCTION =====
    public static class CreateRequest {
        @NotNull(message = "Product ID không được để trống")
        private Long productId;
        @NotNull @DecimalMin("0")
        private BigDecimal startingPrice;
        @NotNull @DecimalMin("0")
        private BigDecimal bidIncrement;
        private BigDecimal reservePrice;
        private BigDecimal buyNowPrice;
        @NotNull
        private LocalDateTime startTime;
        @NotNull
        private LocalDateTime endTime;
        private Integer autoExtendMinutes;
        private Integer snipeThresholdMin;

        public CreateRequest() {}
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public BigDecimal getStartingPrice() { return startingPrice; }
        public void setStartingPrice(BigDecimal startingPrice) { this.startingPrice = startingPrice; }
        public BigDecimal getBidIncrement() { return bidIncrement; }
        public void setBidIncrement(BigDecimal bidIncrement) { this.bidIncrement = bidIncrement; }
        public BigDecimal getReservePrice() { return reservePrice; }
        public void setReservePrice(BigDecimal reservePrice) { this.reservePrice = reservePrice; }
        public BigDecimal getBuyNowPrice() { return buyNowPrice; }
        public void setBuyNowPrice(BigDecimal buyNowPrice) { this.buyNowPrice = buyNowPrice; }
        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
        public Integer getAutoExtendMinutes() { return autoExtendMinutes; }
        public void setAutoExtendMinutes(Integer autoExtendMinutes) { this.autoExtendMinutes = autoExtendMinutes; }
        public Integer getSnipeThresholdMin() { return snipeThresholdMin; }
        public void setSnipeThresholdMin(Integer snipeThresholdMin) { this.snipeThresholdMin = snipeThresholdMin; }
    }

    // ===== BID REQUEST =====
    public static class BidRequest {
        @NotNull @DecimalMin("0")
        private BigDecimal bidAmount;
        private BigDecimal maxAutoBid; // proxy bidding

        public BidRequest() {}
        public BigDecimal getBidAmount() { return bidAmount; }
        public void setBidAmount(BigDecimal bidAmount) { this.bidAmount = bidAmount; }
        public BigDecimal getMaxAutoBid() { return maxAutoBid; }
        public void setMaxAutoBid(BigDecimal maxAutoBid) { this.maxAutoBid = maxAutoBid; }
    }

    // ===== AUCTION RESPONSE =====
    public static class AuctionResponse {
        private Long id;
        private String status;
        private BigDecimal startingPrice;
        private BigDecimal currentPrice;
        private BigDecimal bidIncrement;
        private BigDecimal buyNowPrice;
        private Boolean hasReservePrice;
        private Boolean reserveMet; // đã đạt reserve chưa (không lộ giá reserve)
        private Integer totalBids;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Integer autoExtendMinutes;
        private Integer extendedCount;
        private ProductSummary product;
        private BidInfo winningBid;
        private List<BidInfo> recentBids;
        private LocalDateTime createdAt;

        public AuctionResponse() {}
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public BigDecimal getStartingPrice() { return startingPrice; }
        public void setStartingPrice(BigDecimal startingPrice) { this.startingPrice = startingPrice; }
        public BigDecimal getCurrentPrice() { return currentPrice; }
        public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }
        public BigDecimal getBidIncrement() { return bidIncrement; }
        public void setBidIncrement(BigDecimal bidIncrement) { this.bidIncrement = bidIncrement; }
        public BigDecimal getBuyNowPrice() { return buyNowPrice; }
        public void setBuyNowPrice(BigDecimal buyNowPrice) { this.buyNowPrice = buyNowPrice; }
        public Boolean getHasReservePrice() { return hasReservePrice; }
        public void setHasReservePrice(Boolean hasReservePrice) { this.hasReservePrice = hasReservePrice; }
        public Boolean getReserveMet() { return reserveMet; }
        public void setReserveMet(Boolean reserveMet) { this.reserveMet = reserveMet; }
        public Integer getTotalBids() { return totalBids; }
        public void setTotalBids(Integer totalBids) { this.totalBids = totalBids; }
        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
        public Integer getAutoExtendMinutes() { return autoExtendMinutes; }
        public void setAutoExtendMinutes(Integer autoExtendMinutes) { this.autoExtendMinutes = autoExtendMinutes; }
        public Integer getExtendedCount() { return extendedCount; }
        public void setExtendedCount(Integer extendedCount) { this.extendedCount = extendedCount; }
        public ProductSummary getProduct() { return product; }
        public void setProduct(ProductSummary product) { this.product = product; }
        public BidInfo getWinningBid() { return winningBid; }
        public void setWinningBid(BidInfo winningBid) { this.winningBid = winningBid; }
        public List<BidInfo> getRecentBids() { return recentBids; }
        public void setRecentBids(List<BidInfo> recentBids) { this.recentBids = recentBids; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    public static class BidInfo {
        private Long id;
        private String bidderUsername;
        private BigDecimal bidAmount;
        private Boolean isWinning;
        private LocalDateTime createdAt;

        public BidInfo() {}
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getBidderUsername() { return bidderUsername; }
        public void setBidderUsername(String bidderUsername) { this.bidderUsername = bidderUsername; }
        public BigDecimal getBidAmount() { return bidAmount; }
        public void setBidAmount(BigDecimal bidAmount) { this.bidAmount = bidAmount; }
        public Boolean getIsWinning() { return isWinning; }
        public void setIsWinning(Boolean isWinning) { this.isWinning = isWinning; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    public static class ProductSummary {
        private Long id;
        private String name;
        private String slug;
        private String primaryImage;
        private String shopName;
        private String shopSlug;

        public ProductSummary() {}
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getSlug() { return slug; }
        public void setSlug(String slug) { this.slug = slug; }
        public String getPrimaryImage() { return primaryImage; }
        public void setPrimaryImage(String primaryImage) { this.primaryImage = primaryImage; }
        public String getShopName() { return shopName; }
        public void setShopName(String shopName) { this.shopName = shopName; }
        public String getShopSlug() { return shopSlug; }
        public void setShopSlug(String shopSlug) { this.shopSlug = shopSlug; }
    }
}