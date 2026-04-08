package com.example.carnest.API;

import com.example.carnest.Config.CustomUserDetails;
import com.example.carnest.Model.AuthDTO;
import com.example.carnest.Model.ShowcaseDTO;
import com.example.carnest.Service.ShowcaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/showcases")
@Tag(name = "Showcase", description = "Tủ trưng bày")
public class ShowcaseController {

    @Autowired private ShowcaseService showcaseService;

    @PostMapping
    @Operation(summary = "Tạo tủ trưng bày")
    public ResponseEntity<AuthDTO.MessageResponse> create(
            @AuthenticationPrincipal CustomUserDetails u, @Valid @RequestBody ShowcaseDTO.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(AuthDTO.MessageResponse.builder()
                .status(201).message("Tạo thành công").data(showcaseService.create(u.getUserId(), request)).build());
    }

    @PostMapping("/{id}/items")
    @Operation(summary = "Thêm xe vào tủ")
    public ResponseEntity<AuthDTO.MessageResponse> addItem(
            @AuthenticationPrincipal CustomUserDetails u, @PathVariable Long id, @Valid @RequestBody ShowcaseDTO.AddItemRequest request) {
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder().status(200).message("Đã thêm")
                .data(showcaseService.addItem(u.getUserId(), id, request)).build());
    }

    @PostMapping("/{id}/like")
    @Operation(summary = "Like/Unlike tủ trưng bày")
    public ResponseEntity<AuthDTO.MessageResponse> like(
            @AuthenticationPrincipal CustomUserDetails u, @PathVariable Long id) {
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder().status(200)
                .message(showcaseService.toggleLike(u.getUserId(), id)).build());
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Tủ trưng bày của user (public)")
    public ResponseEntity<AuthDTO.MessageResponse> userShowcases(@PathVariable Long userId) {
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder().status(200).message("Thành công")
                .data(showcaseService.getUserShowcases(userId)).build());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết tủ trưng bày (public)")
    public ResponseEntity<AuthDTO.MessageResponse> getById(
            @PathVariable Long id, @AuthenticationPrincipal CustomUserDetails u) {
        Long uid = u != null ? u.getUserId() : null;
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder().status(200).message("Thành công")
                .data(showcaseService.getById(id, uid)).build());
    }
}