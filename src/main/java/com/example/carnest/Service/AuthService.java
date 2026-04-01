package com.example.carnest.Service;

import com.example.carnest.Entity.User;
import com.example.carnest.Entity.Wallet;
import com.example.carnest.Enum.Role;
import com.example.carnest.Exception.BadRequestException;
import com.example.carnest.Model.AuthDTO;
import com.example.carnest.Repository.UserRepository;
import com.example.carnest.Utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthDTO.AuthResponse register(AuthDTO.RegisterRequest request) {
        // Kiểm tra email đã tồn tại chưa
        if (userRepository.existsByUsername(request.getEmail())) {
            throw new BadRequestException("Tên đăng nhập đã được sử dụng");
        }

        // Tạo user mới
        User user = User.builder()
                .email(request.getEmail().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName().trim())
                .username(request.getUsername().trim())
                .phone(request.getPhone())
                .role(Role.USER)
                .isSeller(false)
                .isVerified(false)
                .isBanned(false)
                .build();

        // Tạo wallet cho user
        Wallet wallet = Wallet.builder()
                .user(user)
                .build();
        user.setWallet(wallet);

        user = userRepository.save(user);

        // Generate tokens
        return buildAuthResponse(user);
    }

    @Transactional
    public AuthDTO.AuthResponse login(AuthDTO.LoginRequest request) {
        // Xác thực username + password
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername().trim(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Tên đăng nhập hoặc mật khẩu không đúng");
        }

        // Lấy user
        User user = userRepository.findByUsername(request.getUsername().trim())
                .orElseThrow(() -> new BadCredentialsException("Tên đăng nhập hoặc mật khẩu không đúng"));

        // Kiểm tra bị ban
        if (user.getIsBanned()) {
            throw new BadRequestException("Tài khoản đã bị khóa. Vui lòng liên hệ hỗ trợ.");
        }

        // Cập nhật last login
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        return buildAuthResponse(user);
    }

    public AuthDTO.AuthResponse refreshToken(AuthDTO.RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        // Validate refresh token
        if (!jwtUtils.validateToken(refreshToken)) {
            throw new BadRequestException("Refresh token không hợp lệ hoặc đã hết hạn");
        }

        // Kiểm tra đúng là refresh token
        String tokenType = jwtUtils.getTokenType(refreshToken);
        if (!"refresh".equals(tokenType)) {
            throw new BadRequestException("Token không phải refresh token");
        }

        // Lấy user từ token
        String email = jwtUtils.getUsernameFromToken(refreshToken);
        User user = userRepository.findByUsername(email)
                .orElseThrow(() -> new BadRequestException("Tên đăng nhập không tồn tại"));

        if (user.getIsBanned()) {
            throw new BadRequestException("Tài khoản đã bị khóa");
        }

        return buildAuthResponse(user);
    }

    public AuthDTO.UserInfo getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("Tên đăng nhập không tồn tại"));

        return buildUserInfo(user);
    }

    // ===== HELPER METHODS =====

    private AuthDTO.AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtUtils.generateAccessToken(user.getId(), user.getUsername());
        String refreshToken = jwtUtils.generateRefreshToken(user.getId(), user.getUsername());

        return AuthDTO.AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtils.getExpirationMs() / 1000)
                .user(buildUserInfo(user))
                .build();
    }

    private AuthDTO.UserInfo buildUserInfo(User user) {
        return AuthDTO.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole().name())
                .isSeller(user.getIsSeller())
                .isVerified(user.getIsVerified())
                .totalBought(user.getTotalBought())
                .totalSold(user.getTotalSold())
                .build();
    }
}
