package com.example.carnest.API;

import com.example.carnest.Config.CustomUserDetails;
import com.example.carnest.Model.AuthDTO;
import com.example.carnest.Model.ReviewDTO;
import com.example.carnest.Service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@Tag(name = "Review", description = "Đánh giá sau đơn hàng")
public class ReviewController {

    @Autowired private ReviewService reviewService;

    @PostMapping
    @Operation(summary = "Đánh giá đơn hàng (sau khi COMPLETED)")
    public ResponseEntity<AuthDTO.MessageResponse> create(
            @AuthenticationPrincipal CustomUserDetails u,
            @Valid @RequestBody ReviewDTO.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(AuthDTO.MessageResponse.builder()
                .status(201).message("Đánh giá thành công")
                .data(reviewService.createReview(u.getUserId(), request)).build());
    }

    @PutMapping("/{id}/reply")
    @Operation(summary = "Phản hồi đánh giá")
    public ResponseEntity<AuthDTO.MessageResponse> reply(
            @AuthenticationPrincipal CustomUserDetails u, @PathVariable Long id,
            @Valid @RequestBody ReviewDTO.ReplyRequest request) {
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message("Phản hồi thành công")
                .data(reviewService.replyReview(u.getUserId(), id, request)).build());
    }

    @GetMapping("/shop/{shopId}")
    @Operation(summary = "Đánh giá của shop (public)")
    public ResponseEntity<AuthDTO.MessageResponse> shopReviews(
            @PathVariable Long shopId, @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message("Thành công")
                .data(reviewService.getShopReviews(shopId, cursor, size)).build());
    }
}