package com.example.carnest.Model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

public class AuthDTO {

    // ===== REQUEST =====

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterRequest {

        @NotBlank(message = "Họ tên không được để trống")
        @Size(max = 150, message = "Họ tên tối đa 150 ký tự")
        private String fullName;

        @NotBlank(message = "Tên đăng nhập không được để trống")
        @Size(max = 150, message = "Tên đăng nhập tối đa 150 ký tự")
        private String username;

        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không hợp lệ")
        private String email;

        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(min = 6, max = 100, message = "Mật khẩu từ 6 đến 100 ký tự")
        private String password;

        @Size(max = 20, message = "Số điện thoại tối đa 20 ký tự")
        private String phone;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {

        @NotBlank(message = "Tên đăng nhập không được để trống")
        private String username;

        @NotBlank(message = "Mật khẩu không được để trống")
        private String password;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RefreshTokenRequest {

        @NotBlank(message = "Refresh token không được để trống")
        private String refreshToken;
    }

    // ===== RESPONSE =====

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthResponse {
        private String accessToken;
        private String refreshToken;
        private String tokenType;
        private Long expiresIn;
        private UserInfo user;

        public static AuthResponseBuilder builder() {
            return new AuthResponseBuilder();
        }

        public static class AuthResponseBuilder {
            private String accessToken;
            private String refreshToken;
            private String tokenType;
            private Long expiresIn;
            private UserInfo user;

            public AuthResponseBuilder accessToken(String accessToken) { this.accessToken = accessToken; return this; }
            public AuthResponseBuilder refreshToken(String refreshToken) { this.refreshToken = refreshToken; return this; }
            public AuthResponseBuilder tokenType(String tokenType) { this.tokenType = tokenType; return this; }
            public AuthResponseBuilder expiresIn(Long expiresIn) { this.expiresIn = expiresIn; return this; }
            public AuthResponseBuilder user(UserInfo user) { this.user = user; return this; }

            public AuthResponse build() {
                return new AuthResponse(accessToken, refreshToken, tokenType, expiresIn, user);
            }
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String username;
        private String email;
        private String fullName;
        private String phone;
        private String avatarUrl;
        private String role;
        private Boolean isSeller;
        private Boolean isVerified;
        private Integer totalBought;
        private Integer totalSold;

        public static UserInfoBuilder builder() {
            return new UserInfoBuilder();
        }

        public static class UserInfoBuilder {
            private Long id;
            private String username;
            private String email;
            private String fullName;
            private String phone;
            private String avatarUrl;
            private String role;
            private Boolean isSeller;
            private Boolean isVerified;
            private Integer totalBought;
            private Integer totalSold;

            public UserInfoBuilder id(Long id) { this.id = id; return this; }
            public UserInfoBuilder username(String username) { this.username = username; return this; }
            public UserInfoBuilder email(String email) { this.email = email; return this; }
            public UserInfoBuilder fullName(String fullName) { this.fullName = fullName; return this; }
            public UserInfoBuilder phone(String phone) { this.phone = phone; return this; }
            public UserInfoBuilder avatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; return this; }
            public UserInfoBuilder role(String role) { this.role = role; return this; }
            public UserInfoBuilder isSeller(Boolean isSeller) { this.isSeller = isSeller; return this; }
            public UserInfoBuilder isVerified(Boolean isVerified) { this.isVerified = isVerified; return this; }
            public UserInfoBuilder totalBought(Integer totalBought) { this.totalBought = totalBought; return this; }
            public UserInfoBuilder totalSold(Integer totalSold) { this.totalSold = totalSold; return this; }

            public UserInfo build() {
                return new UserInfo(id, username, email, fullName, phone, avatarUrl, role, isSeller, isVerified, totalBought, totalSold);
            }
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageResponse {
        private int status;
        private String message;
        private Object data;

        public static MessageResponseBuilder builder() {
            return new MessageResponseBuilder();
        }

        public static class MessageResponseBuilder {
            private int status;
            private String message;
            private Object data;

            public MessageResponseBuilder status(int status) { this.status = status; return this; }
            public MessageResponseBuilder message(String message) { this.message = message; return this; }
            public MessageResponseBuilder data(Object data) { this.data = data; return this; }

            public MessageResponse build() {
                return new MessageResponse(status, message, data);
            }
        }
    }
}