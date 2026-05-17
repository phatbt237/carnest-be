package com.example.carnest.API;

import com.example.carnest.Model.AdminReportDTO;
import com.example.carnest.Model.AuthDTO;
import com.example.carnest.Service.AdminReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/reports")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Report", description = "Báo cáo & thống kê hệ thống (Admin only)")
public class AdminReportController {

    private final AdminReportService adminReportService;

    @Autowired
    public AdminReportController(AdminReportService adminReportService) {
        this.adminReportService = adminReportService;
    }

    // ===== TỔNG QUAN HỆ THỐNG =====
    @GetMapping("/overview")
    @Operation(summary = "Tổng quan hệ thống",
               description = "KPIs: tổng user, sản phẩm, đơn hàng, doanh thu, shop, đấu giá, review, báo cáo vi phạm")
    public ResponseEntity<AuthDTO.MessageResponse> overview() {
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message("Thành công")
                .data(adminReportService.getOverview()).build());
    }

    // ===== DOANH THU THEO THỜI GIAN =====
    @GetMapping("/revenue")
    @Operation(summary = "Doanh thu theo thời gian",
               description = "Truyền from/to (ISO datetime) và groupBy=day|month. Mặc định 30 ngày gần nhất theo ngày.")
    public ResponseEntity<AuthDTO.MessageResponse> revenue(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "day") String groupBy) {

        LocalDateTime now = LocalDateTime.now();
        if (from == null) from = now.minusDays(30);
        if (to == null) to = now;

        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message("Thành công")
                .data(adminReportService.getRevenueSeries(from, to, groupBy)).build());
    }

    // ===== THỐNG KÊ ĐƠN HÀNG =====
    @GetMapping("/orders")
    @Operation(summary = "Thống kê đơn hàng",
               description = "Phân bổ đơn hàng theo từng trạng thái và tỷ lệ phần trăm")
    public ResponseEntity<AuthDTO.MessageResponse> orders() {
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message("Thành công")
                .data(adminReportService.getOrderStats()).build());
    }

    // ===== THỐNG KÊ USER =====
    @GetMapping("/users")
    @Operation(summary = "Thống kê người dùng",
               description = "Tổng user, seller, admin, banned + biểu đồ tăng trưởng. groupBy=day|month")
    public ResponseEntity<AuthDTO.MessageResponse> users(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "day") String groupBy) {

        LocalDateTime now = LocalDateTime.now();
        if (from == null) from = now.minusDays(30);
        if (to == null) to = now;

        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message("Thành công")
                .data(adminReportService.getUserStats(from, to, groupBy)).build());
    }

    // ===== THỐNG KÊ SẢN PHẨM =====
    @GetMapping("/products")
    @Operation(summary = "Thống kê sản phẩm",
               description = "Tổng sản phẩm, phân bổ theo trạng thái và category")
    public ResponseEntity<AuthDTO.MessageResponse> products() {
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message("Thành công")
                .data(adminReportService.getProductStats()).build());
    }

    // ===== TOP SHOP THEO DOANH THU =====
    @GetMapping("/top-shops")
    @Operation(summary = "Top shop theo doanh thu",
               description = "Danh sách shop có doanh thu cao nhất từ đơn COMPLETED. Tối đa 50.")
    public ResponseEntity<AuthDTO.MessageResponse> topShops(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message("Thành công")
                .data(adminReportService.getTopShops(limit)).build());
    }

    // ===== TOP SẢN PHẨM BÁN CHẠY =====
    @GetMapping("/top-products/sold")
    @Operation(summary = "Top sản phẩm bán chạy nhất",
               description = "Xếp theo soldCount giảm dần. Tối đa 50.")
    public ResponseEntity<AuthDTO.MessageResponse> topProductsBySold(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message("Thành công")
                .data(adminReportService.getTopProductsBySold(limit)).build());
    }

    // ===== TOP SẢN PHẨM ĐƯỢC XEM NHIỀU =====
    @GetMapping("/top-products/viewed")
    @Operation(summary = "Top sản phẩm được xem nhiều nhất",
               description = "Xếp theo viewCount giảm dần, chỉ lấy sản phẩm ACTIVE. Tối đa 50.")
    public ResponseEntity<AuthDTO.MessageResponse> topProductsByView(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message("Thành công")
                .data(adminReportService.getTopProductsByView(limit)).build());
    }

    // ===== TOP BUYER =====
    @GetMapping("/top-buyers")
    @Operation(summary = "Top người mua theo tổng chi tiêu",
               description = "Tính trên đơn COMPLETED. Tối đa 50.")
    public ResponseEntity<AuthDTO.MessageResponse> topBuyers(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message("Thành công")
                .data(adminReportService.getTopBuyers(limit)).build());
    }

    // ===== THỐNG KÊ ĐẤU GIÁ =====
    @GetMapping("/auctions")
    @Operation(summary = "Thống kê đấu giá",
               description = "Tổng số auction, phân bổ theo trạng thái, tổng giá trị auction đã kết thúc")
    public ResponseEntity<AuthDTO.MessageResponse> auctions() {
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message("Thành công")
                .data(adminReportService.getAuctionStats()).build());
    }

    // ===== THỐNG KÊ VI PHẠM =====
    @GetMapping("/violations")
    @Operation(summary = "Thống kê báo cáo vi phạm",
               description = "Tổng số report, phân bổ theo trạng thái, lý do và loại đối tượng bị report")
    public ResponseEntity<AuthDTO.MessageResponse> violations() {
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message("Thành công")
                .data(adminReportService.getViolationStats()).build());
    }

    // ===== THỐNG KÊ REVIEW =====
    @GetMapping("/reviews")
    @Operation(summary = "Thống kê đánh giá",
               description = "Tổng review, điểm trung bình, phân bổ 1-5 sao")
    public ResponseEntity<AuthDTO.MessageResponse> reviews() {
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message("Thành công")
                .data(adminReportService.getReviewStats()).build());
    }
}
