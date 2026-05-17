package com.example.carnest.Model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class AdminReportDTO {

    // ===== TỔNG QUAN HỆ THỐNG =====
    public static class OverviewResponse {
        // Users
        private long totalUsers;
        private long totalSellers;
        private long totalBanned;
        private long newUsersLast30Days;

        // Sản phẩm
        private long totalProductsActive;
        private long totalProductsSold;
        private long totalProductsPending;

        // Đơn hàng
        private long totalOrders;
        private long totalOrdersCompleted;
        private long totalOrdersCancelled;
        private long totalOrdersPendingPayment;
        private long ordersLast30Days;

        // Doanh thu
        private BigDecimal totalRevenue;
        private BigDecimal revenueLast30Days;

        // Shop
        private long totalShops;
        private long activeShops;

        // Đấu giá
        private long totalAuctions;
        private long activeAuctions;

        // Review & Report
        private long totalReviews;
        private double avgRatingOverall;
        private long pendingReports;

        public OverviewResponse() {}
        public long getTotalUsers() { return totalUsers; }
        public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }
        public long getTotalSellers() { return totalSellers; }
        public void setTotalSellers(long totalSellers) { this.totalSellers = totalSellers; }
        public long getTotalBanned() { return totalBanned; }
        public void setTotalBanned(long totalBanned) { this.totalBanned = totalBanned; }
        public long getNewUsersLast30Days() { return newUsersLast30Days; }
        public void setNewUsersLast30Days(long newUsersLast30Days) { this.newUsersLast30Days = newUsersLast30Days; }
        public long getTotalProductsActive() { return totalProductsActive; }
        public void setTotalProductsActive(long totalProductsActive) { this.totalProductsActive = totalProductsActive; }
        public long getTotalProductsSold() { return totalProductsSold; }
        public void setTotalProductsSold(long totalProductsSold) { this.totalProductsSold = totalProductsSold; }
        public long getTotalProductsPending() { return totalProductsPending; }
        public void setTotalProductsPending(long totalProductsPending) { this.totalProductsPending = totalProductsPending; }
        public long getTotalOrders() { return totalOrders; }
        public void setTotalOrders(long totalOrders) { this.totalOrders = totalOrders; }
        public long getTotalOrdersCompleted() { return totalOrdersCompleted; }
        public void setTotalOrdersCompleted(long totalOrdersCompleted) { this.totalOrdersCompleted = totalOrdersCompleted; }
        public long getTotalOrdersCancelled() { return totalOrdersCancelled; }
        public void setTotalOrdersCancelled(long totalOrdersCancelled) { this.totalOrdersCancelled = totalOrdersCancelled; }
        public long getTotalOrdersPendingPayment() { return totalOrdersPendingPayment; }
        public void setTotalOrdersPendingPayment(long totalOrdersPendingPayment) { this.totalOrdersPendingPayment = totalOrdersPendingPayment; }
        public long getOrdersLast30Days() { return ordersLast30Days; }
        public void setOrdersLast30Days(long ordersLast30Days) { this.ordersLast30Days = ordersLast30Days; }
        public BigDecimal getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }
        public BigDecimal getRevenueLast30Days() { return revenueLast30Days; }
        public void setRevenueLast30Days(BigDecimal revenueLast30Days) { this.revenueLast30Days = revenueLast30Days; }
        public long getTotalShops() { return totalShops; }
        public void setTotalShops(long totalShops) { this.totalShops = totalShops; }
        public long getActiveShops() { return activeShops; }
        public void setActiveShops(long activeShops) { this.activeShops = activeShops; }
        public long getTotalAuctions() { return totalAuctions; }
        public void setTotalAuctions(long totalAuctions) { this.totalAuctions = totalAuctions; }
        public long getActiveAuctions() { return activeAuctions; }
        public void setActiveAuctions(long activeAuctions) { this.activeAuctions = activeAuctions; }
        public long getTotalReviews() { return totalReviews; }
        public void setTotalReviews(long totalReviews) { this.totalReviews = totalReviews; }
        public double getAvgRatingOverall() { return avgRatingOverall; }
        public void setAvgRatingOverall(double avgRatingOverall) { this.avgRatingOverall = avgRatingOverall; }
        public long getPendingReports() { return pendingReports; }
        public void setPendingReports(long pendingReports) { this.pendingReports = pendingReports; }
    }

    // ===== DOANH THU THEO THỜI GIAN =====
    public static class RevenueSeriesResponse {
        private String groupBy; // day | month
        private List<RevenuePoint> series;
        private BigDecimal totalRevenue;
        private long totalOrders;

        public RevenueSeriesResponse() {}
        public String getGroupBy() { return groupBy; }
        public void setGroupBy(String groupBy) { this.groupBy = groupBy; }
        public List<RevenuePoint> getSeries() { return series; }
        public void setSeries(List<RevenuePoint> series) { this.series = series; }
        public BigDecimal getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }
        public long getTotalOrders() { return totalOrders; }
        public void setTotalOrders(long totalOrders) { this.totalOrders = totalOrders; }
    }

    public static class RevenuePoint {
        private String period;
        private BigDecimal revenue;
        private long orderCount;

        public RevenuePoint() {}
        public RevenuePoint(String period, BigDecimal revenue, long orderCount) {
            this.period = period;
            this.revenue = revenue;
            this.orderCount = orderCount;
        }
        public String getPeriod() { return period; }
        public void setPeriod(String period) { this.period = period; }
        public BigDecimal getRevenue() { return revenue; }
        public void setRevenue(BigDecimal revenue) { this.revenue = revenue; }
        public long getOrderCount() { return orderCount; }
        public void setOrderCount(long orderCount) { this.orderCount = orderCount; }
    }

    // ===== THỐNG KÊ ĐƠN HÀNG =====
    public static class OrderStatsResponse {
        private List<StatusCount> byStatus;
        private long totalOrders;

        public OrderStatsResponse() {}
        public List<StatusCount> getByStatus() { return byStatus; }
        public void setByStatus(List<StatusCount> byStatus) { this.byStatus = byStatus; }
        public long getTotalOrders() { return totalOrders; }
        public void setTotalOrders(long totalOrders) { this.totalOrders = totalOrders; }
    }

    public static class StatusCount {
        private String status;
        private long count;
        private double percentage;

        public StatusCount() {}
        public StatusCount(String status, long count, double percentage) {
            this.status = status; this.count = count; this.percentage = percentage;
        }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public long getCount() { return count; }
        public void setCount(long count) { this.count = count; }
        public double getPercentage() { return percentage; }
        public void setPercentage(double percentage) { this.percentage = percentage; }
    }

    // ===== THỐNG KÊ USER =====
    public static class UserStatsResponse {
        private long totalUsers;
        private long totalSellers;
        private long totalAdmins;
        private long totalBanned;
        private long newUsersLast7Days;
        private long newUsersLast30Days;
        private List<GrowthPoint> growthSeries;
        private String groupBy;

        public UserStatsResponse() {}
        public long getTotalUsers() { return totalUsers; }
        public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }
        public long getTotalSellers() { return totalSellers; }
        public void setTotalSellers(long totalSellers) { this.totalSellers = totalSellers; }
        public long getTotalAdmins() { return totalAdmins; }
        public void setTotalAdmins(long totalAdmins) { this.totalAdmins = totalAdmins; }
        public long getTotalBanned() { return totalBanned; }
        public void setTotalBanned(long totalBanned) { this.totalBanned = totalBanned; }
        public long getNewUsersLast7Days() { return newUsersLast7Days; }
        public void setNewUsersLast7Days(long newUsersLast7Days) { this.newUsersLast7Days = newUsersLast7Days; }
        public long getNewUsersLast30Days() { return newUsersLast30Days; }
        public void setNewUsersLast30Days(long newUsersLast30Days) { this.newUsersLast30Days = newUsersLast30Days; }
        public List<GrowthPoint> getGrowthSeries() { return growthSeries; }
        public void setGrowthSeries(List<GrowthPoint> growthSeries) { this.growthSeries = growthSeries; }
        public String getGroupBy() { return groupBy; }
        public void setGroupBy(String groupBy) { this.groupBy = groupBy; }
    }

    public static class GrowthPoint {
        private String period;
        private long count;

        public GrowthPoint() {}
        public GrowthPoint(String period, long count) { this.period = period; this.count = count; }
        public String getPeriod() { return period; }
        public void setPeriod(String period) { this.period = period; }
        public long getCount() { return count; }
        public void setCount(long count) { this.count = count; }
    }

    // ===== THỐNG KÊ SẢN PHẨM =====
    public static class ProductStatsResponse {
        private long totalProducts;
        private long activeProducts;
        private long soldProducts;
        private long pendingVerification;
        private long newProductsLast30Days;
        private List<StatusCount> byStatus;
        private List<CategoryCount> byCategory;

        public ProductStatsResponse() {}
        public long getTotalProducts() { return totalProducts; }
        public void setTotalProducts(long totalProducts) { this.totalProducts = totalProducts; }
        public long getActiveProducts() { return activeProducts; }
        public void setActiveProducts(long activeProducts) { this.activeProducts = activeProducts; }
        public long getSoldProducts() { return soldProducts; }
        public void setSoldProducts(long soldProducts) { this.soldProducts = soldProducts; }
        public long getPendingVerification() { return pendingVerification; }
        public void setPendingVerification(long pendingVerification) { this.pendingVerification = pendingVerification; }
        public long getNewProductsLast30Days() { return newProductsLast30Days; }
        public void setNewProductsLast30Days(long newProductsLast30Days) { this.newProductsLast30Days = newProductsLast30Days; }
        public List<StatusCount> getByStatus() { return byStatus; }
        public void setByStatus(List<StatusCount> byStatus) { this.byStatus = byStatus; }
        public List<CategoryCount> getByCategory() { return byCategory; }
        public void setByCategory(List<CategoryCount> byCategory) { this.byCategory = byCategory; }
    }

    public static class CategoryCount {
        private String category;
        private long count;

        public CategoryCount() {}
        public CategoryCount(String category, long count) { this.category = category; this.count = count; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public long getCount() { return count; }
        public void setCount(long count) { this.count = count; }
    }

    // ===== TOP SHOP =====
    public static class TopShopEntry {
        private Long shopId;
        private String shopName;
        private BigDecimal revenue;
        private long orderCount;
        private int totalSold;
        private int followerCount;
        private BigDecimal ratingAvg;

        public TopShopEntry() {}
        public Long getShopId() { return shopId; }
        public void setShopId(Long shopId) { this.shopId = shopId; }
        public String getShopName() { return shopName; }
        public void setShopName(String shopName) { this.shopName = shopName; }
        public BigDecimal getRevenue() { return revenue; }
        public void setRevenue(BigDecimal revenue) { this.revenue = revenue; }
        public long getOrderCount() { return orderCount; }
        public void setOrderCount(long orderCount) { this.orderCount = orderCount; }
        public int getTotalSold() { return totalSold; }
        public void setTotalSold(int totalSold) { this.totalSold = totalSold; }
        public int getFollowerCount() { return followerCount; }
        public void setFollowerCount(int followerCount) { this.followerCount = followerCount; }
        public BigDecimal getRatingAvg() { return ratingAvg; }
        public void setRatingAvg(BigDecimal ratingAvg) { this.ratingAvg = ratingAvg; }
    }

    // ===== TOP SẢN PHẨM =====
    public static class TopProductEntry {
        private Long productId;
        private String productName;
        private int soldCount;
        private int viewCount;
        private BigDecimal price;
        private String shopName;

        public TopProductEntry() {}
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public int getSoldCount() { return soldCount; }
        public void setSoldCount(int soldCount) { this.soldCount = soldCount; }
        public int getViewCount() { return viewCount; }
        public void setViewCount(int viewCount) { this.viewCount = viewCount; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public String getShopName() { return shopName; }
        public void setShopName(String shopName) { this.shopName = shopName; }
    }

    // ===== TOP BUYER =====
    public static class TopBuyerEntry {
        private Long userId;
        private String username;
        private String fullName;
        private BigDecimal totalSpent;
        private long orderCount;

        public TopBuyerEntry() {}
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public BigDecimal getTotalSpent() { return totalSpent; }
        public void setTotalSpent(BigDecimal totalSpent) { this.totalSpent = totalSpent; }
        public long getOrderCount() { return orderCount; }
        public void setOrderCount(long orderCount) { this.orderCount = orderCount; }
    }

    // ===== THỐNG KÊ ĐẤU GIÁ =====
    public static class AuctionStatsResponse {
        private long totalAuctions;
        private long activeAuctions;
        private long endedAuctions;
        private long upcomingAuctions;
        private long cancelledAuctions;
        private BigDecimal totalEndedValue;
        private List<StatusCount> byStatus;

        public AuctionStatsResponse() {}
        public long getTotalAuctions() { return totalAuctions; }
        public void setTotalAuctions(long totalAuctions) { this.totalAuctions = totalAuctions; }
        public long getActiveAuctions() { return activeAuctions; }
        public void setActiveAuctions(long activeAuctions) { this.activeAuctions = activeAuctions; }
        public long getEndedAuctions() { return endedAuctions; }
        public void setEndedAuctions(long endedAuctions) { this.endedAuctions = endedAuctions; }
        public long getUpcomingAuctions() { return upcomingAuctions; }
        public void setUpcomingAuctions(long upcomingAuctions) { this.upcomingAuctions = upcomingAuctions; }
        public long getCancelledAuctions() { return cancelledAuctions; }
        public void setCancelledAuctions(long cancelledAuctions) { this.cancelledAuctions = cancelledAuctions; }
        public BigDecimal getTotalEndedValue() { return totalEndedValue; }
        public void setTotalEndedValue(BigDecimal totalEndedValue) { this.totalEndedValue = totalEndedValue; }
        public List<StatusCount> getByStatus() { return byStatus; }
        public void setByStatus(List<StatusCount> byStatus) { this.byStatus = byStatus; }
    }

    // ===== THỐNG KÊ VI PHẠM =====
    public static class ViolationStatsResponse {
        private long totalReports;
        private long pendingReports;
        private long resolvedReports;
        private long rejectedReports;
        private List<ReasonCount> byReason;
        private List<TargetCount> byTargetType;

        public ViolationStatsResponse() {}
        public long getTotalReports() { return totalReports; }
        public void setTotalReports(long totalReports) { this.totalReports = totalReports; }
        public long getPendingReports() { return pendingReports; }
        public void setPendingReports(long pendingReports) { this.pendingReports = pendingReports; }
        public long getResolvedReports() { return resolvedReports; }
        public void setResolvedReports(long resolvedReports) { this.resolvedReports = resolvedReports; }
        public long getRejectedReports() { return rejectedReports; }
        public void setRejectedReports(long rejectedReports) { this.rejectedReports = rejectedReports; }
        public List<ReasonCount> getByReason() { return byReason; }
        public void setByReason(List<ReasonCount> byReason) { this.byReason = byReason; }
        public List<TargetCount> getByTargetType() { return byTargetType; }
        public void setByTargetType(List<TargetCount> byTargetType) { this.byTargetType = byTargetType; }
    }

    public static class ReasonCount {
        private String reason;
        private long count;

        public ReasonCount() {}
        public ReasonCount(String reason, long count) { this.reason = reason; this.count = count; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public long getCount() { return count; }
        public void setCount(long count) { this.count = count; }
    }

    public static class TargetCount {
        private String targetType;
        private long count;

        public TargetCount() {}
        public TargetCount(String targetType, long count) { this.targetType = targetType; this.count = count; }
        public String getTargetType() { return targetType; }
        public void setTargetType(String targetType) { this.targetType = targetType; }
        public long getCount() { return count; }
        public void setCount(long count) { this.count = count; }
    }

    // ===== THỐNG KÊ REVIEW =====
    public static class ReviewStatsResponse {
        private long totalReviews;
        private double avgRatingOverall;
        private Map<Integer, Long> ratingDistribution;

        public ReviewStatsResponse() {}
        public long getTotalReviews() { return totalReviews; }
        public void setTotalReviews(long totalReviews) { this.totalReviews = totalReviews; }
        public double getAvgRatingOverall() { return avgRatingOverall; }
        public void setAvgRatingOverall(double avgRatingOverall) { this.avgRatingOverall = avgRatingOverall; }
        public Map<Integer, Long> getRatingDistribution() { return ratingDistribution; }
        public void setRatingDistribution(Map<Integer, Long> ratingDistribution) { this.ratingDistribution = ratingDistribution; }
    }
}
