package com.example.carnest.API;

import com.example.carnest.Config.CustomUserDetails;
import com.example.carnest.Model.AuthDTO;
import com.example.carnest.Model.WantListDTO;
import com.example.carnest.Service.WantListService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wantlist")
@Tag(name = "WantList", description = "Tìm xe hộ tôi")
public class WantListController {

    @Autowired private WantListService wantListService;

    @PostMapping
    @Operation(summary = "Đăng yêu cầu tìm xe")
    public ResponseEntity<AuthDTO.MessageResponse> create(
            @AuthenticationPrincipal CustomUserDetails u,
            @Valid @RequestBody WantListDTO.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(AuthDTO.MessageResponse.builder()
                .status(201).message("Đăng yêu cầu thành công")
                .data(wantListService.create(u.getUserId(), request)).build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật yêu cầu tìm xe")
    public ResponseEntity<AuthDTO.MessageResponse> update(
            @AuthenticationPrincipal CustomUserDetails u, @PathVariable Long id,
            @Valid @RequestBody WantListDTO.CreateRequest request) {
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message("Cập nhật thành công")
                .data(wantListService.update(u.getUserId(), id, request)).build());
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Hủy yêu cầu tìm xe")
    public ResponseEntity<AuthDTO.MessageResponse> cancel(
            @AuthenticationPrincipal CustomUserDetails u, @PathVariable Long id) {
        wantListService.cancel(u.getUserId(), id);
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message("Đã hủy yêu cầu").build());
    }

    @GetMapping("/my")
    @Operation(summary = "Yêu cầu tìm xe của tôi")
    public ResponseEntity<AuthDTO.MessageResponse> my(
            @AuthenticationPrincipal CustomUserDetails u,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message("Thành công")
                .data(wantListService.getMyWantList(u.getUserId(), cursor, size)).build());
    }

    @GetMapping("/public")
    @Operation(summary = "Yêu cầu tìm xe công khai (public)")
    public ResponseEntity<AuthDTO.MessageResponse> publicList(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message("Thành công")
                .data(wantListService.getPublicWantList(cursor, size)).build());
    }
}