package com.example.carnest.API;

import com.example.carnest.Config.CustomUserDetails;
import com.example.carnest.Enum.OrderStatus;
import com.example.carnest.Model.AuthDTO;
import com.example.carnest.Model.OrderDTO;
import com.example.carnest.Model.ShopDTO;
import com.example.carnest.Service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Order", description = "Quản lý đơn hàng")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // ===== CHECKOUT =====
    @PostMapping("/checkout")
    @Operation(summary = "Đặt hàng từ giỏ", description = "Tự động gom đơn theo shop. Gửi productIds để checkout 1 phần giỏ, null = toàn bộ.")
    public ResponseEntity<AuthDTO.MessageResponse> checkout(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody OrderDTO.CheckoutRequest request) {

        List<OrderDTO.OrderResponse> orders = orderService.checkout(userDetails.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AuthDTO.MessageResponse.builder()
                        .status(201).message("Đặt hàng thành công — " + orders.size() + " đơn")
                        .data(orders).build());
    }

    @PutMapping("/{id}/pay")
    @Operation(summary = "Thanh toán đơn hàng", description = "Phải thanh toán trong 5 phút sau khi đặt")
    public ResponseEntity<AuthDTO.MessageResponse> payOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message("Thanh toán thành công")
                .data(orderService.payOrder(userDetails.getUserId(), id)).build());
    }

    // ===== XEM CHI TIẾT ĐƠN =====
    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết đơn hàng")
    public ResponseEntity<AuthDTO.MessageResponse> getById(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {

        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message("Thành công")
                .data(orderService.getOrderById(userDetails.getUserId(), id)).build());
    }

    // ===== DANH SÁCH ĐƠN CỦA TÔI (buyer) =====
    @GetMapping("/my")
    @Operation(summary = "Đơn hàng của tôi (buyer)")
    public ResponseEntity<AuthDTO.MessageResponse> getMyOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size) {

        ShopDTO.CursorPage<OrderDTO.OrderResponse> page =
                orderService.getMyOrders(userDetails.getUserId(), status, cursor, size);
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message("Thành công").data(page).build());
    }

    // ===== DANH SÁCH ĐƠN CỦA SHOP (seller) =====
    @GetMapping("/shop")
    @Operation(summary = "Đơn hàng shop nhận được (seller)")
    public ResponseEntity<AuthDTO.MessageResponse> getShopOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size) {

        ShopDTO.CursorPage<OrderDTO.OrderResponse> page =
                orderService.getShopOrders(userDetails.getUserId(), status, cursor, size);
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message("Thành công").data(page).build());
    }

    // ===== SELLER XÁC NHẬN ĐƠN =====
    @PutMapping("/{id}/confirm")
    @Operation(summary = "Seller xác nhận đơn hàng")
    public ResponseEntity<AuthDTO.MessageResponse> confirm(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @RequestBody(required = false) OrderDTO.UpdateStatusRequest request) {

        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message("Đã xác nhận đơn hàng")
                .data(orderService.confirmOrder(userDetails.getUserId(), id, request)).build());
    }

    // ===== SELLER GỬI HÀNG =====
    @PutMapping("/{id}/ship")
    @Operation(summary = "Seller gửi hàng", description = "Gửi kèm tracking number")
    public ResponseEntity<AuthDTO.MessageResponse> ship(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @RequestBody(required = false) OrderDTO.UpdateStatusRequest request) {

        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message("Đã gửi hàng")
                .data(orderService.shipOrder(userDetails.getUserId(), id, request)).build());
    }

    // ===== BUYER XÁC NHẬN ĐÃ NHẬN =====
    @PutMapping("/{id}/delivered")
    @Operation(summary = "Buyer xác nhận đã nhận hàng")
    public ResponseEntity<AuthDTO.MessageResponse> delivered(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {

        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message("Đã xác nhận nhận hàng")
                .data(orderService.confirmDelivered(userDetails.getUserId(), id)).build());
    }

    // ===== BUYER HOÀN THÀNH ĐƠN =====
    @PutMapping("/{id}/complete")
    @Operation(summary = "Buyer hoàn thành đơn — giải phóng tiền cho seller")
    public ResponseEntity<AuthDTO.MessageResponse> complete(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {

        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message("Đơn hàng hoàn thành")
                .data(orderService.completeOrder(userDetails.getUserId(), id)).build());
    }

    // ===== HỦY ĐƠN =====
    @PutMapping("/{id}/cancel")
    @Operation(summary = "Hủy đơn hàng", description = "Buyer hoặc Seller đều có thể hủy khi đơn ở trạng thái PENDING/CONFIRMED")
    public ResponseEntity<AuthDTO.MessageResponse> cancel(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @RequestBody(required = false) OrderDTO.UpdateStatusRequest request) {

        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message("Đã hủy đơn hàng")
                .data(orderService.cancelOrder(userDetails.getUserId(), id, request)).build());
    }

    // ===== XEM SỐ DƯ VÍ =====
    @GetMapping("/wallet")
    @Operation(summary = "Xem số dư ví")
    public ResponseEntity<AuthDTO.MessageResponse> getWallet(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message("Thành công")
                .data(orderService.getWallet(userDetails.getUserId())).build());
    }

    // ===== NẠP TIỀN TEST =====
    @PostMapping("/wallet/deposit")
    @Operation(summary = "Nạp tiền vào ví (test)", description = "Dùng để test, production sẽ qua cổng thanh toán")
    public ResponseEntity<AuthDTO.MessageResponse> deposit(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam BigDecimal amount) {
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message("Nạp tiền thành công")
                .data(orderService.deposit(userDetails.getUserId(), amount)).build());
    }

    // ===== LỊCH SỬ GIAO DỊCH VÍ =====
    @GetMapping("/wallet/transactions")
    @Operation(summary = "Lịch sử giao dịch ví")
    public ResponseEntity<AuthDTO.MessageResponse> getWalletTransactions(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message("Thành công")
                .data(orderService.getWalletTransactions(userDetails.getUserId(), cursor, size)).build());
    }
}
