package com.example.carnest.API;

import com.example.carnest.Config.CustomUserDetails;
import com.example.carnest.Model.AuthDTO;
import com.example.carnest.Service.BadgeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/badges")
@Tag(name = "Badge", description = "Huy hiệu & Danh hiệu")
public class BadgeController {

    @Autowired private BadgeService badgeService;

    @GetMapping
    @Operation(summary = "Tất cả badge (public)")
    public ResponseEntity<AuthDTO.MessageResponse> all() {
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder().status(200).message("Thành công")
                .data(badgeService.getAllBadges()).build());
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Badge của user (public)")
    public ResponseEntity<AuthDTO.MessageResponse> userBadges(@PathVariable Long userId) {
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder().status(200).message("Thành công")
                .data(badgeService.getUserBadges(userId)).build());
    }

    @GetMapping("/my")
    @Operation(summary = "Badge của tôi")
    public ResponseEntity<AuthDTO.MessageResponse> myBadges(@AuthenticationPrincipal CustomUserDetails u) {
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder().status(200).message("Thành công")
                .data(badgeService.getUserBadges(u.getUserId())).build());
    }
}