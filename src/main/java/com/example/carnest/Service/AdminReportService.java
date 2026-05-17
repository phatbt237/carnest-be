package com.example.carnest.Service;

import com.example.carnest.Enum.*;
import com.example.carnest.Model.AdminReportDTO;
import com.example.carnest.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminReportService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ShopRepository shopRepository;
    private final ReviewRepository reviewRepository;
    private final ReportRepository reportRepository;
    private final AuctionRepository auctionRepository;
    private final AuctionBidRepository auctionBidRepository;

    @Autowired
    public AdminReportService(OrderRepository orderRepository, UserRepository userRepository,
                              ProductRepository productRepository, ShopRepository shopRepository,
                              ReviewRepository reviewRepository, ReportRepository reportRepository,
                              AuctionRepository auctionRepository, AuctionBidRepository auctionBidRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.shopRepository = shopRepository;
        this.reviewRepository = reviewRepository;
        this.reportRepository = reportRepository;
        this.auctionRepository = auctionRepository;
        this.auctionBidRepository = auctionBidRepository;
    }

    // ===== TỔNG QUAN =====
    public AdminReportDTO.OverviewResponse getOverview() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thirtyDaysAgo = now.minusDays(30);

        AdminReportDTO.OverviewResponse r = new AdminReportDTO.OverviewResponse();

        // Users
        r.setTotalUsers(userRepository.count());
        r.setTotalSellers(userRepository.countSellers());
        r.setTotalBanned(userRepository.countBanned());
        r.setNewUsersLast30Days(userRepository.countNewUsers(thirtyDaysAgo, now));

        // Products
        r.setTotalProductsActive(productRepository.countByStatus(ProductStatus.ACTIVE));
        r.setTotalProductsSold(productRepository.countByStatus(ProductStatus.SOLD));
        r.setTotalProductsPending(productRepository.countByStatus(ProductStatus.RESERVED));

        // Orders
        r.setTotalOrders(orderRepository.count());
        r.setTotalOrdersCompleted(orderRepository.countByStatus(OrderStatus.COMPLETED));
        r.setTotalOrdersCancelled(orderRepository.countByStatus(OrderStatus.CANCELLED));
        r.setTotalOrdersPendingPayment(orderRepository.countByStatus(OrderStatus.PENDING_PAYMENT));
        r.setOrdersLast30Days(orderRepository.countByPeriod(thirtyDaysAgo, now));

        // Revenue
        r.setTotalRevenue(orderRepository.sumTotalRevenue());
        r.setRevenueLast30Days(orderRepository.sumRevenueByPeriod(thirtyDaysAgo, now));

        // Shops
        r.setTotalShops(shopRepository.count());
        r.setActiveShops(shopRepository.countActiveShops());

        // Auctions
        r.setTotalAuctions(auctionRepository.count());
        r.setActiveAuctions(auctionRepository.countByStatus(AuctionStatus.ACTIVE));

        // Reviews & Reports
        r.setTotalReviews(reviewRepository.countTotal());
        Double avgRating = reviewRepository.avgRatingOverall();
        r.setAvgRatingOverall(avgRating != null ? round2(avgRating) : 0.0);
        r.setPendingReports(reportRepository.countByStatus(ReportStatus.PENDING));

        return r;
    }

    // ===== DOANH THU THEO THỜI GIAN =====
    public AdminReportDTO.RevenueSeriesResponse getRevenueSeries(
            LocalDateTime from, LocalDateTime to, String groupBy) {

        List<Object[]> rows = "month".equalsIgnoreCase(groupBy)
                ? orderRepository.revenueGroupByMonth(from, to)
                : orderRepository.revenueGroupByDay(from, to);

        List<AdminReportDTO.RevenuePoint> series = new ArrayList<>();
        BigDecimal totalRevenue = BigDecimal.ZERO;
        long totalOrders = 0;

        for (Object[] row : rows) {
            String period = (String) row[0];
            BigDecimal revenue = toBigDecimal(row[1]);
            long count = toLong(row[2]);
            series.add(new AdminReportDTO.RevenuePoint(period, revenue, count));
            totalRevenue = totalRevenue.add(revenue);
            totalOrders += count;
        }

        AdminReportDTO.RevenueSeriesResponse r = new AdminReportDTO.RevenueSeriesResponse();
        r.setGroupBy(groupBy);
        r.setSeries(series);
        r.setTotalRevenue(totalRevenue);
        r.setTotalOrders(totalOrders);
        return r;
    }

    // ===== THỐNG KÊ ĐƠN HÀNG =====
    public AdminReportDTO.OrderStatsResponse getOrderStats() {
        List<Object[]> rows = orderRepository.countGroupByStatus();
        long total = rows.stream().mapToLong(r -> toLong(r[1])).sum();

        List<AdminReportDTO.StatusCount> byStatus = new ArrayList<>();
        for (Object[] row : rows) {
            String status = row[0].toString();
            long count = toLong(row[1]);
            double pct = total > 0 ? round2((double) count / total * 100) : 0.0;
            byStatus.add(new AdminReportDTO.StatusCount(status, count, pct));
        }

        AdminReportDTO.OrderStatsResponse r = new AdminReportDTO.OrderStatsResponse();
        r.setByStatus(byStatus);
        r.setTotalOrders(total);
        return r;
    }

    // ===== THỐNG KÊ USER =====
    public AdminReportDTO.UserStatsResponse getUserStats(
            LocalDateTime from, LocalDateTime to, String groupBy) {

        LocalDateTime now = LocalDateTime.now();

        AdminReportDTO.UserStatsResponse r = new AdminReportDTO.UserStatsResponse();
        r.setTotalUsers(userRepository.count());
        r.setTotalSellers(userRepository.countSellers());
        r.setTotalAdmins(userRepository.countByRole(Role.ADMIN));
        r.setTotalBanned(userRepository.countBanned());
        r.setNewUsersLast7Days(userRepository.countNewUsers(now.minusDays(7), now));
        r.setNewUsersLast30Days(userRepository.countNewUsers(now.minusDays(30), now));

        List<Object[]> rows = "month".equalsIgnoreCase(groupBy)
                ? userRepository.userGrowthByMonth(from, to)
                : userRepository.userGrowthByDay(from, to);

        List<AdminReportDTO.GrowthPoint> series = new ArrayList<>();
        for (Object[] row : rows) {
            series.add(new AdminReportDTO.GrowthPoint((String) row[0], toLong(row[1])));
        }
        r.setGrowthSeries(series);
        r.setGroupBy(groupBy);
        return r;
    }

    // ===== THỐNG KÊ SẢN PHẨM =====
    public AdminReportDTO.ProductStatsResponse getProductStats() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        AdminReportDTO.ProductStatsResponse r = new AdminReportDTO.ProductStatsResponse();
        r.setTotalProducts(productRepository.count());
        r.setActiveProducts(productRepository.countByStatus(ProductStatus.ACTIVE));
        r.setSoldProducts(productRepository.countByStatus(ProductStatus.SOLD));
        r.setPendingVerification(productRepository.countByStatus(ProductStatus.RESERVED));
        r.setNewProductsLast30Days(productRepository.countNewProducts(thirtyDaysAgo, LocalDateTime.now()));

        // By status
        List<Object[]> statusRows = productRepository.countGroupByStatus();
        long totalProducts = statusRows.stream().mapToLong(row -> toLong(row[1])).sum();
        List<AdminReportDTO.StatusCount> byStatus = new ArrayList<>();
        for (Object[] row : statusRows) {
            String status = row[0].toString();
            long count = toLong(row[1]);
            double pct = totalProducts > 0 ? round2((double) count / totalProducts * 100) : 0.0;
            byStatus.add(new AdminReportDTO.StatusCount(status, count, pct));
        }
        r.setByStatus(byStatus);

        // By category
        List<Object[]> catRows = productRepository.countGroupByCategory();
        List<AdminReportDTO.CategoryCount> byCategory = new ArrayList<>();
        for (Object[] row : catRows) {
            String cat = row[0] != null ? row[0].toString() : "Chưa phân loại";
            byCategory.add(new AdminReportDTO.CategoryCount(cat, toLong(row[1])));
        }
        r.setByCategory(byCategory);

        return r;
    }

    // ===== TOP SHOP THEO DOANH THU =====
    public List<AdminReportDTO.TopShopEntry> getTopShops(int limit) {
        limit = Math.min(Math.max(limit, 1), 50);
        List<Object[]> rows = orderRepository.topShopsByRevenue(limit);

        List<AdminReportDTO.TopShopEntry> result = new ArrayList<>();
        int max = Math.min(rows.size(), limit);
        for (int i = 0; i < max; i++) {
            Object[] row = rows.get(i);
            AdminReportDTO.TopShopEntry entry = new AdminReportDTO.TopShopEntry();
            entry.setShopId(toLong(row[0]));
            entry.setShopName(row[1] != null ? row[1].toString() : "");
            entry.setRevenue(toBigDecimal(row[2]));
            entry.setOrderCount(toLong(row[3]));

            // Bổ sung thêm info từ Shop entity
            shopRepository.findByIdWithUser(entry.getShopId()).ifPresent(shop -> {
                entry.setTotalSold(shop.getTotalSold());
                entry.setFollowerCount(shop.getFollowerCount());
                entry.setRatingAvg(shop.getRatingAvg());
            });
            result.add(entry);
        }
        return result;
    }

    // ===== TOP SẢN PHẨM BÁN CHẠY =====
    public List<AdminReportDTO.TopProductEntry> getTopProductsBySold(int limit) {
        limit = Math.min(Math.max(limit, 1), 50);
        List<Object[]> rows = productRepository.topBySoldCount(limit);

        List<AdminReportDTO.TopProductEntry> result = new ArrayList<>();
        int max = Math.min(rows.size(), limit);
        for (int i = 0; i < max; i++) {
            Object[] row = rows.get(i);
            AdminReportDTO.TopProductEntry entry = new AdminReportDTO.TopProductEntry();
            entry.setProductId(toLong(row[0]));
            entry.setProductName(row[1] != null ? row[1].toString() : "");
            entry.setSoldCount(toInt(row[2]));
            entry.setViewCount(toInt(row[3]));
            entry.setPrice(toBigDecimal(row[4]));
            entry.setShopName(row[5] != null ? row[5].toString() : "");
            result.add(entry);
        }
        return result;
    }

    // ===== TOP SẢN PHẨM ĐƯỢC XEM NHIỀU =====
    public List<AdminReportDTO.TopProductEntry> getTopProductsByView(int limit) {
        limit = Math.min(Math.max(limit, 1), 50);
        List<Object[]> rows = productRepository.topByViewCount(limit);

        List<AdminReportDTO.TopProductEntry> result = new ArrayList<>();
        int max = Math.min(rows.size(), limit);
        for (int i = 0; i < max; i++) {
            Object[] row = rows.get(i);
            AdminReportDTO.TopProductEntry entry = new AdminReportDTO.TopProductEntry();
            entry.setProductId(toLong(row[0]));
            entry.setProductName(row[1] != null ? row[1].toString() : "");
            entry.setSoldCount(toInt(row[2]));
            entry.setViewCount(toInt(row[3]));
            entry.setPrice(toBigDecimal(row[4]));
            entry.setShopName(row[5] != null ? row[5].toString() : "");
            result.add(entry);
        }
        return result;
    }

    // ===== TOP BUYER =====
    public List<AdminReportDTO.TopBuyerEntry> getTopBuyers(int limit) {
        limit = Math.min(Math.max(limit, 1), 50);
        List<Object[]> rows = orderRepository.topBuyersBySpend(limit);

        List<AdminReportDTO.TopBuyerEntry> result = new ArrayList<>();
        int max = Math.min(rows.size(), limit);
        for (int i = 0; i < max; i++) {
            Object[] row = rows.get(i);
            AdminReportDTO.TopBuyerEntry entry = new AdminReportDTO.TopBuyerEntry();
            entry.setUserId(toLong(row[0]));
            entry.setUsername(row[1] != null ? row[1].toString() : "");
            entry.setFullName(row[2] != null ? row[2].toString() : "");
            entry.setTotalSpent(toBigDecimal(row[3]));
            entry.setOrderCount(toLong(row[4]));
            result.add(entry);
        }
        return result;
    }

    // ===== THỐNG KÊ ĐẤU GIÁ =====
    public AdminReportDTO.AuctionStatsResponse getAuctionStats() {
        AdminReportDTO.AuctionStatsResponse r = new AdminReportDTO.AuctionStatsResponse();
        r.setTotalAuctions(auctionRepository.count());
        r.setActiveAuctions(auctionRepository.countByStatus(AuctionStatus.ACTIVE));
        r.setEndedAuctions(auctionRepository.countByStatus(AuctionStatus.ENDED)
                + auctionRepository.countByStatus(AuctionStatus.COMPLETED));
        r.setUpcomingAuctions(auctionRepository.countByStatus(AuctionStatus.UPCOMING));
        r.setCancelledAuctions(auctionRepository.countByStatus(AuctionStatus.CANCELLED)
                + auctionRepository.countByStatus(AuctionStatus.NO_SALE));
        r.setTotalEndedValue(auctionRepository.sumEndedAuctionValue());

        List<Object[]> rows = auctionRepository.countGroupByStatus();
        long total = rows.stream().mapToLong(row -> toLong(row[1])).sum();
        List<AdminReportDTO.StatusCount> byStatus = new ArrayList<>();
        for (Object[] row : rows) {
            String status = row[0].toString();
            long count = toLong(row[1]);
            double pct = total > 0 ? round2((double) count / total * 100) : 0.0;
            byStatus.add(new AdminReportDTO.StatusCount(status, count, pct));
        }
        r.setByStatus(byStatus);
        return r;
    }

    // ===== THỐNG KÊ VI PHẠM =====
    public AdminReportDTO.ViolationStatsResponse getViolationStats() {
        AdminReportDTO.ViolationStatsResponse r = new AdminReportDTO.ViolationStatsResponse();
        r.setTotalReports(reportRepository.count());
        r.setPendingReports(reportRepository.countByStatus(ReportStatus.PENDING)
                + reportRepository.countByStatus(ReportStatus.REVIEWING));
        r.setResolvedReports(reportRepository.countByStatus(ReportStatus.RESOLVED));
        r.setRejectedReports(reportRepository.countByStatus(ReportStatus.DISMISSED));

        List<AdminReportDTO.ReasonCount> byReason = new ArrayList<>();
        for (Object[] row : reportRepository.countGroupByReason()) {
            byReason.add(new AdminReportDTO.ReasonCount(row[0].toString(), toLong(row[1])));
        }
        r.setByReason(byReason);

        List<AdminReportDTO.TargetCount> byTarget = new ArrayList<>();
        for (Object[] row : reportRepository.countGroupByTargetType()) {
            byTarget.add(new AdminReportDTO.TargetCount(row[0].toString(), toLong(row[1])));
        }
        r.setByTargetType(byTarget);

        return r;
    }

    // ===== THỐNG KÊ REVIEW =====
    public AdminReportDTO.ReviewStatsResponse getReviewStats() {
        AdminReportDTO.ReviewStatsResponse r = new AdminReportDTO.ReviewStatsResponse();
        r.setTotalReviews(reviewRepository.countTotal());
        Double avg = reviewRepository.avgRatingOverall();
        r.setAvgRatingOverall(avg != null ? round2(avg) : 0.0);

        Map<Integer, Long> dist = new LinkedHashMap<>();
        for (int i = 1; i <= 5; i++) dist.put(i, 0L);
        for (Object[] row : reviewRepository.countGroupByRating()) {
            int rating = toInt(row[0]);
            dist.put(rating, toLong(row[1]));
        }
        r.setRatingDistribution(dist);
        return r;
    }

    // ===== HELPERS =====
    private BigDecimal toBigDecimal(Object obj) {
        if (obj == null) return BigDecimal.ZERO;
        if (obj instanceof BigDecimal) return (BigDecimal) obj;
        return new BigDecimal(obj.toString());
    }

    private long toLong(Object obj) {
        if (obj == null) return 0L;
        if (obj instanceof Long) return (Long) obj;
        if (obj instanceof Number) return ((Number) obj).longValue();
        return Long.parseLong(obj.toString());
    }

    private int toInt(Object obj) {
        if (obj == null) return 0;
        if (obj instanceof Integer) return (Integer) obj;
        if (obj instanceof Number) return ((Number) obj).intValue();
        return Integer.parseInt(obj.toString());
    }

    private double round2(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
