package com.example.carnest.API;

import com.example.carnest.Config.CustomUserDetails;
import com.example.carnest.Config.RateLimit;
import com.example.carnest.Model.AuthDTO;
import com.example.carnest.Model.ChatDTO;
import com.example.carnest.Service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@Tag(name = "Chat", description = "Nhắn tin")
public class ChatController {

    @Autowired private ChatService chatService;

    @PostMapping("/send/{receiverId}")
    @Operation(summary = "Gửi tin nhắn")
    @RateLimit(action = "send_chat", limit = 20, windowSeconds = 60)
    public ResponseEntity<AuthDTO.MessageResponse> send(
            @AuthenticationPrincipal CustomUserDetails u, @PathVariable Long receiverId,
            @RequestBody ChatDTO.SendMessageRequest request) {
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder().status(200).message("Đã gửi")
                .data(chatService.sendMessage(u.getUserId(), receiverId, request.getContent(),
                        request.getImageUrls(), request.getTagType(), request.getTagId())).build());
    }

    @GetMapping("/conversations")
    @Operation(summary = "Danh sách cuộc trò chuyện")
    public ResponseEntity<AuthDTO.MessageResponse> conversations(
            @AuthenticationPrincipal CustomUserDetails u,
            @RequestParam(required = false) String cursor, @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder().status(200).message("Thành công")
                .data(chatService.getConversations(u.getUserId(), cursor, size)).build());
    }

    @GetMapping("/conversations/{id}/messages")
    @Operation(summary = "Tin nhắn trong cuộc trò chuyện")
    public ResponseEntity<AuthDTO.MessageResponse> messages(
            @AuthenticationPrincipal CustomUserDetails u, @PathVariable Long id,
            @RequestParam(required = false) String cursor, @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder().status(200).message("Thành công")
                .data(chatService.getMessages(u.getUserId(), id, cursor, size)).build());
    }

    @PutMapping("/conversations/{id}/read")
    @Operation(summary = "Đánh dấu đã đọc")
    public ResponseEntity<AuthDTO.MessageResponse> markRead(
            @AuthenticationPrincipal CustomUserDetails u, @PathVariable Long id) {
        chatService.markRead(u.getUserId(), id);
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder().status(200).message("Đã đánh dấu đọc").build());
    }
}