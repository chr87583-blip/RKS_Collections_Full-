package com.rks.collections.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

public class AuthDto {

    @Data
    public static class LoginRequest {
        @NotBlank private String username;
        @NotBlank private String password;
    }

    @Data
    public static class RegisterRequest {
        @NotBlank @Size(min=3, max=50) private String username;
        @NotBlank @Size(min=6) private String password;
        @NotBlank @Email private String email;
        private String fullName;
        private String phone;
    }

    @Data
    public static class AuthResponse {
        private String token;
        private String username;
        private String role;
        private String fullName;
        private long unreadNotifications;

        public AuthResponse(String token, String username, String role, String fullName, long unread) {
            this.token = token;
            this.username = username;
            this.role = role;
            this.fullName = fullName;
            this.unreadNotifications = unread;
        }
    }

    @Data
    public static class ChatRequest {
        @NotBlank private String message;
        private String productContext; // optional: product ID for context-aware AI
    }

    @Data
    public static class NotificationRequest {
        @NotBlank private String title;
        @NotBlank private String message;
        private String type = "GENERAL";
        private String productId;
        private Long userId; // null = broadcast
    }
}
