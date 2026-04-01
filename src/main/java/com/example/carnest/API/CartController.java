package com.example.carnest.API;

import com.example.carnest.Config.CustomUserDetails;
import com.example.carnest.Model.AuthDTO;
import com.example.carnest.Model.CartDTO;
import com.example.carnest.Service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@Tag(name = "Cart", description = "Giỏ hàng")
public class CartController {

    private final CartService cartService;

    @Autowired
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    @Operation(summary = "Xem giỏ hàng")
    public ResponseEntity<AuthDTO.MessageResponse> getCart(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message("Thành công")
                .data(cartService.getCart(userDetails.getUserId())).build());
    }

    @PostMapping
    @Operation(summary = "Thêm sản phẩm vào giỏ")
    public ResponseEntity<AuthDTO.MessageResponse> addToCart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CartDTO.AddToCartRequest request) {
        String msg = cartService.addToCart(userDetails.getUserId(), request);
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message(msg).build());
    }

    @DeleteMapping("/{productId}")
    @Operation(summary = "Xóa sản phẩm khỏi giỏ")
    public ResponseEntity<AuthDTO.MessageResponse> removeFromCart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long productId) {
        String msg = cartService.removeFromCart(userDetails.getUserId(), productId);
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message(msg).build());
    }

    @DeleteMapping
    @Operation(summary = "Xóa toàn bộ giỏ hàng")
    public ResponseEntity<AuthDTO.MessageResponse> clearCart(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String msg = cartService.clearCart(userDetails.getUserId());
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message(msg).build());
    }
}
