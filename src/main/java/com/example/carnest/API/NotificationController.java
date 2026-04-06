package com.example.carnest.API;

import com.example.carnest.Config.CustomUserDetails;
import com.example.carnest.Model.AuthDTO;
import com.example.carnest.Service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notification", description = "Thông báo")
public class NotificationController {

    @Autowired private NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Danh sách thông báo")
    public ResponseEntity<AuthDTO.MessageResponse> list(
            @AuthenticationPrincipal CustomUserDetails u,
            @RequestParam(required = false) String cursor, @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder().status(200).message("Thành công")
                .data(notificationService.getNotifications(u.getUserId(), cursor, size)).build());
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Số thông báo chưa đọc")
    public ResponseEntity<AuthDTO.MessageResponse> unread(@AuthenticationPrincipal CustomUserDetails u) {
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder().status(200).message("Thành công")
                .data(notificationService.getUnreadCount(u.getUserId())).build());
    }

    @PutMapping("/read-all")
    @Operation(summary = "Đánh dấu tất cả đã đọc")
    public ResponseEntity<AuthDTO.MessageResponse> readAll(@AuthenticationPrincipal CustomUserDetails u) {
        notificationService.markAllRead(u.getUserId());
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder().status(200).message("Đã đánh dấu tất cả đã đọc").build());
    }
}