package com.rks.collections.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 1000)
    private String message;

    private String type; // NEW_ARRIVAL, SALE, RESTOCK, GENERAL, ORDER_STATUS

    private String productId; // if linked to a product

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private AppUser user; // null = broadcast to all

    private boolean isRead = false;
    private boolean isBroadcast = false;

    private LocalDateTime createdAt = LocalDateTime.now();

    public Notification(String title, String message, String type) {
        this.title = title;
        this.message = message;
        this.type = type;
        this.isBroadcast = true;
    }

    public Notification(String title, String message, String type, AppUser user) {
        this.title = title;
        this.message = message;
        this.type = type;
        this.user = user;
        this.isBroadcast = false;
    }
}
