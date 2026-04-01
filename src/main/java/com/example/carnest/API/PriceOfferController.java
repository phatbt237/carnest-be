package com.example.carnest.API;

import com.example.carnest.Config.CustomUserDetails;
import com.example.carnest.Model.AuthDTO;
import com.example.carnest.Model.PriceOfferDTO;
import com.example.carnest.Service.PriceOfferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/offers")
@Tag(name = "Price Offer", description = "Đề xuất giá / thương lượng")
public class PriceOfferController {

    private final PriceOfferService offerService;

    @Autowired
    public PriceOfferController(PriceOfferService offerService) {
        this.offerService = offerService;
    }

    @PostMapping
    @Operation(summary = "Gửi đề xuất giá (buyer)")
    public ResponseEntity<AuthDTO.MessageResponse> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PriceOfferDTO.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AuthDTO.MessageResponse.builder().status(201).message("Gửi đề xuất giá thành công")
                        .data(offerService.createOffer(userDetails.getUserId(), request)).build());
    }

    @PutMapping("/{id}/accept")
    @Operation(summary = "Seller chấp nhận offer")
    public ResponseEntity<AuthDTO.MessageResponse> accept(
            @AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id) {
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder().status(200).message("Đã chấp nhận offer")
                .data(offerService.accept(userDetails.getUserId(), id)).build());
    }

    @PutMapping("/{id}/reject")
    @Operation(summary = "Seller từ chối offer")
    public ResponseEntity<AuthDTO.MessageResponse> reject(
            @AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id) {
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder().status(200).message("Đã từ chối offer")
                .data(offerService.reject(userDetails.getUserId(), id)).build());
    }

    @PutMapping("/{id}/counter")
    @Operation(summary = "Seller đề xuất giá ngược")
    public ResponseEntity<AuthDTO.MessageResponse> counter(
            @AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id,
            @Valid @RequestBody PriceOfferDTO.CounterRequest request) {
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder().status(200).message("Đã gửi counter offer")
                .data(offerService.counter(userDetails.getUserId(), id, request)).build());
    }

    @PutMapping("/{id}/accept-counter")
    @Operation(summary = "Buyer chấp nhận counter offer")
    public ResponseEntity<AuthDTO.MessageResponse> acceptCounter(
            @AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id) {
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder().status(200).message("Đã chấp nhận counter offer")
                .data(offerService.acceptCounter(userDetails.getUserId(), id)).build());
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Buyer hủy offer")
    public ResponseEntity<AuthDTO.MessageResponse> cancel(
            @AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id) {
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder().status(200).message("Đã hủy offer")
                .data(offerService.cancelOffer(userDetails.getUserId(), id)).build());
    }

    @GetMapping("/my")
    @Operation(summary = "Offer tôi đã gửi (buyer)")
    public ResponseEntity<AuthDTO.MessageResponse> myOffers(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder().status(200).message("Thành công")
                .data(offerService.getMyOffers(userDetails.getUserId(), cursor, size)).build());
    }

    @GetMapping("/shop")
    @Operation(summary = "Offer shop nhận được (seller)")
    public ResponseEntity<AuthDTO.MessageResponse> shopOffers(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder().status(200).message("Thành công")
                .data(offerService.getShopOffers(userDetails.getUserId(), cursor, size)).build());
    }
}