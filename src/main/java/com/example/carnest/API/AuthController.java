package com.example.carnest.API;

import com.example.carnest.Config.CustomUserDetails;
import com.example.carnest.Model.AuthDTO;
import com.example.carnest.Service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthDTO.MessageResponse> register(
            @Valid @RequestBody AuthDTO.RegisterRequest request) {

        AuthDTO.AuthResponse authResponse = authService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AuthDTO.MessageResponse.builder()
                        .status(201)
                        .message("Đăng ký thành công")
                        .data(authResponse)
                        .build());
    }

    @PostMapping("/login")
    public ResponseEntity<AuthDTO.MessageResponse> login(
            @Valid @RequestBody AuthDTO.LoginRequest request) {

        AuthDTO.AuthResponse authResponse = authService.login(request);

        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200)
                .message("Đăng nhập thành công")
                .data(authResponse)
                .build());
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthDTO.MessageResponse> refreshToken(
            @Valid @RequestBody AuthDTO.RefreshTokenRequest request) {

        AuthDTO.AuthResponse authResponse = authService.refreshToken(request);

        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200)
                .message("Token đã được làm mới")
                .data(authResponse)
                .build());
    }

    @GetMapping("/me")
    public ResponseEntity<AuthDTO.MessageResponse> getCurrentUser(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        AuthDTO.UserInfo userInfo = authService.getCurrentUser(userDetails.getUserId());

        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200)
                .message("Thành công")
                .data(userInfo)
                .build());
    }
}