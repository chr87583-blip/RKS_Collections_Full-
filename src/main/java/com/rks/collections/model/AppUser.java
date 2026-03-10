package com.rks.collections.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "app_users")
@Data
@NoArgsConstructor
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    private String fullName;
    private String phone;

    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    private boolean enabled = true;
    private boolean notificationsEnabled = true;

    // FCM push token for mobile
    private String fcmToken;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime lastLogin;

    // FCM device tokens (multiple devices)
    @ElementCollection
    @CollectionTable(name = "user_fcm_tokens", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "fcm_token")
    private List<String> fcmTokens;

    public enum Role {
        USER, ADMIN
    }
}
