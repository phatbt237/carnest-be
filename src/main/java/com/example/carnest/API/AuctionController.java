package com.example.carnest.API;

import com.example.carnest.Config.CustomUserDetails;
import com.example.carnest.Model.AuctionDTO;
import com.example.carnest.Model.AuthDTO;
import com.example.carnest.Model.ShopDTO;
import com.example.carnest.Service.AuctionService;
import com.example.carnest.Service.OrderScheduler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auctions")
@Tag(name = "Auction", description = "Đấu giá xe mô hình")
public class AuctionController {

    private final AuctionService auctionService;
    private final OrderScheduler orderScheduler;

    @Autowired
    public AuctionController(AuctionService auctionService, OrderScheduler orderScheduler) {
        this.auctionService = auctionService;
        this.orderScheduler = orderScheduler;
    }

    @PostMapping
    @Operation(summary = "Tạo phiên đấu giá (seller)")
    public ResponseEntity<AuthDTO.MessageResponse> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AuctionDTO.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AuthDTO.MessageResponse.builder().status(201).message("Tạo phiên đấu giá thành công")
                        .data(auctionService.create(userDetails.getUserId(), request)).build());
    }

    @PostMapping("/{id}/bid")
    @Operation(summary = "Đặt bid")
    public ResponseEntity<AuthDTO.MessageResponse> bid(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody AuctionDTO.BidRequest request) {
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder().status(200).message("Đặt bid thành công")
                .data(auctionService.placeBid(userDetails.getUserId(), id, request)).build());
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Hủy phiên đấu giá (seller, chưa có bid)")
    public ResponseEntity<AuthDTO.MessageResponse> cancel(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder().status(200).message("Đã hủy phiên đấu giá")
                .data(auctionService.cancel(userDetails.getUserId(), id)).build());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết phiên đấu giá (public)")
    public ResponseEntity<AuthDTO.MessageResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder().status(200).message("Thành công")
                .data(auctionService.getById(id)).build());
    }

    @GetMapping
    @Operation(summary = "Danh sách đấu giá (public)", description = "filter: active (default), ending_soon, upcoming, ended")
    public ResponseEntity<AuthDTO.MessageResponse> list(
            @RequestParam(defaultValue = "active") String filter,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder().status(200).message("Thành công")
                .data(auctionService.getAuctions(filter, cursor, size)).build());
    }

    @GetMapping("/test-end-auction")
    public String test() {
        orderScheduler.endExpiredAuctions();
        return "ok";
    }
}