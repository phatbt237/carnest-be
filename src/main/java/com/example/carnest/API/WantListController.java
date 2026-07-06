package com.example.carnest.API;

import com.example.carnest.Config.CustomUserDetails;
import com.example.carnest.Config.RateLimit;
import com.example.carnest.Enum.TagType;
import com.example.carnest.Exception.BadRequestException;
import com.example.carnest.Model.AuthDTO;
import com.example.carnest.Model.ChatDTO;
import com.example.carnest.Model.WantListDTO;
import com.example.carnest.Service.ChatService;
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
    @Autowired private ChatService chatService;

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

    @PostMapping("/{id}/contact")
    @Operation(summary = "Liên hệ với người đăng yêu cầu tìm xe (dành cho người bán)")
    @RateLimit(action = "wantlist_contact", limit = 20, windowSeconds = 60)
    public ResponseEntity<AuthDTO.MessageResponse> contact(
            @AuthenticationPrincipal CustomUserDetails u, @PathVariable Long id,
            @RequestBody ChatDTO.SendMessageRequest request) {
        Long ownerId = wantListService.getById(id).getUser().getId();
        if (ownerId.equals(u.getUserId())) {
            throw new BadRequestException("Bạn không thể tự liên hệ với chính yêu cầu tìm xe của mình");
        }
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder().status(200).message("Đã gửi")
                .data(chatService.sendMessage(u.getUserId(), ownerId, request.getContent(),
                        request.getImageUrls(), TagType.WANT_LIST, id)).build());
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