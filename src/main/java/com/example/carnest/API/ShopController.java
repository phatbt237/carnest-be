package com.example.carnest.API;

import com.example.carnest.Config.CustomUserDetails;
import com.example.carnest.Model.AuthDTO;
import com.example.carnest.Model.ShopDTO;
import com.example.carnest.Service.ShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shops")
@Tag(name = "Shop", description = "Quản lý cửa hàng")
public class ShopController {

    private final ShopService shopService;

    @Autowired
    public ShopController(ShopService shopService) {
        this.shopService = shopService;
    }

    // ===== TẠO SHOP =====
    @PostMapping
    @Operation(summary = "Tạo shop mới", description = "Mỗi user chỉ tạo được 1 shop")
    public ResponseEntity<AuthDTO.MessageResponse> createShop(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ShopDTO.CreateShopRequest request) {

        ShopDTO.ShopResponse shop = shopService.createShop(userDetails.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AuthDTO.MessageResponse.builder()
                        .status(201).message("Tạo shop thành công").data(shop).build());
    }

    // ===== CẬP NHẬT SHOP =====
    @PutMapping
    @Operation(summary = "Cập nhật shop của tôi")
    public ResponseEntity<AuthDTO.MessageResponse> updateShop(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ShopDTO.UpdateShopRequest request) {

        ShopDTO.ShopResponse shop = shopService.updateShop(userDetails.getUserId(), request);
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message("Cập nhật shop thành công").data(shop).build());
    }

    // ===== XEM SHOP CỦA TÔI =====
    @GetMapping("/me")
    @Operation(summary = "Xem shop của tôi")
    public ResponseEntity<AuthDTO.MessageResponse> getMyShop(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        ShopDTO.ShopResponse shop = shopService.getMyShop(userDetails.getUserId());
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message("Thành công").data(shop).build());
    }

    // ===== XEM SHOP THEO SLUG =====
    @GetMapping("/slug/{slug}")
    @Operation(summary = "Xem shop theo slug (public)")
    public ResponseEntity<AuthDTO.MessageResponse> getShopBySlug(
            @PathVariable String slug,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long currentUserId = (userDetails != null) ? userDetails.getUserId() : null;
        ShopDTO.ShopResponse shop = shopService.getShopBySlug(slug, currentUserId);
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message("Thành công").data(shop).build());
    }

    // ===== XEM SHOP THEO ID =====
    @GetMapping("/{id}")
    @Operation(summary = "Xem shop theo ID (public)")
    public ResponseEntity<AuthDTO.MessageResponse> getShopById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long currentUserId = (userDetails != null) ? userDetails.getUserId() : null;
        ShopDTO.ShopResponse shop = shopService.getShopById(id, currentUserId);
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message("Thành công").data(shop).build());
    }

    // ===== DANH SÁCH SHOP — cursor-based =====
    @GetMapping
    @Operation(summary = "Danh sách shop (cursor-based)",
               description = "sortBy: follower (default), rating, newest. " +
                             "Lần đầu không gửi cursor. Response trả nextCursor, gửi lại để lấy trang tiếp.")
    public ResponseEntity<AuthDTO.MessageResponse> getShops(
            @Parameter(description = "follower | rating | newest")
            @RequestParam(defaultValue = "follower") String sortBy,
            @Parameter(description = "Cursor từ response trước (null = trang đầu)")
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long currentUserId = (userDetails != null) ? userDetails.getUserId() : null;
        ShopDTO.CursorPage<ShopDTO.ShopSummary> page = shopService.getShops(sortBy, cursor, size, currentUserId);

        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message("Thành công").data(page).build());
    }

    // ===== TÌM KIẾM SHOP — cursor-based =====
    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm shop theo tên (cursor-based)")
    public ResponseEntity<AuthDTO.MessageResponse> searchShops(
            @RequestParam String keyword,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long currentUserId = (userDetails != null) ? userDetails.getUserId() : null;
        ShopDTO.CursorPage<ShopDTO.ShopSummary> page = shopService.searchShops(keyword, cursor, size, currentUserId);

        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message("Thành công").data(page).build());
    }

    // ===== FOLLOW SHOP =====
    @PostMapping("/{shopId}/follow")
    @Operation(summary = "Follow shop")
    public ResponseEntity<AuthDTO.MessageResponse> followShop(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long shopId) {

        String message = shopService.followShop(userDetails.getUserId(), shopId);
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message(message).build());
    }

    // ===== UNFOLLOW SHOP =====
    @DeleteMapping("/{shopId}/follow")
    @Operation(summary = "Unfollow shop")
    public ResponseEntity<AuthDTO.MessageResponse> unfollowShop(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long shopId) {

        String message = shopService.unfollowShop(userDetails.getUserId(), shopId);
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message(message).build());
    }

    // ===== DANH SÁCH SHOP ĐANG FOLLOW — cursor-based =====
    @GetMapping("/following")
    @Operation(summary = "Danh sách shop tôi đang follow (cursor-based)")
    public ResponseEntity<AuthDTO.MessageResponse> getFollowingShops(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size) {

        ShopDTO.CursorPage<ShopDTO.ShopSummary> page =
                shopService.getFollowingShops(userDetails.getUserId(), cursor, size);

        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message("Thành công").data(page).build());
    }
}
