package com.example.carnest.API;

import com.example.carnest.Config.CustomUserDetails;
import com.example.carnest.Model.AuthDTO;
import com.example.carnest.Model.TradeDTO;
import com.example.carnest.Service.TradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trades")
@Tag(name = "Trade", description = "Đổi xe")
public class TradeController {

    @Autowired private TradeService tradeService;

    @PostMapping
    @Operation(summary = "Đề xuất đổi xe")
    public ResponseEntity<AuthDTO.MessageResponse> create(
            @AuthenticationPrincipal CustomUserDetails u,
            @Valid @RequestBody TradeDTO.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(AuthDTO.MessageResponse.builder()
                .status(201).message("Đề xuất đổi xe thành công")
                .data(tradeService.create(u.getUserId(), request)).build());
    }

    @PutMapping("/{id}/accept")
    @Operation(summary = "Chấp nhận đổi xe")
    public ResponseEntity<AuthDTO.MessageResponse> accept(
            @AuthenticationPrincipal CustomUserDetails u, @PathVariable Long id) {
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message("Đã chấp nhận đổi xe")
                .data(tradeService.accept(u.getUserId(), id)).build());
    }

    @PutMapping("/{id}/reject")
    @Operation(summary = "Từ chối đổi xe")
    public ResponseEntity<AuthDTO.MessageResponse> reject(
            @AuthenticationPrincipal CustomUserDetails u, @PathVariable Long id) {
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message("Đã từ chối")
                .data(tradeService.reject(u.getUserId(), id)).build());
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Hủy đề xuất đổi xe")
    public ResponseEntity<AuthDTO.MessageResponse> cancel(
            @AuthenticationPrincipal CustomUserDetails u, @PathVariable Long id) {
        tradeService.cancel(u.getUserId(), id);
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message("Đã hủy đề xuất").build());
    }

    @GetMapping("/my")
    @Operation(summary = "Đề xuất đổi xe tôi gửi")
    public ResponseEntity<AuthDTO.MessageResponse> my(
            @AuthenticationPrincipal CustomUserDetails u) {
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message("Thành công")
                .data(tradeService.getMyTrades(u.getUserId())).build());
    }

    @GetMapping("/received")
    @Operation(summary = "Đề xuất đổi xe tôi nhận")
    public ResponseEntity<AuthDTO.MessageResponse> received(
            @AuthenticationPrincipal CustomUserDetails u) {
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message("Thành công")
                .data(tradeService.getReceivedTrades(u.getUserId())).build());
    }
}