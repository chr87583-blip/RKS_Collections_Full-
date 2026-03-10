package com.rks.collections.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Data
@NoArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private AppUser user;

    private String role; // "user" or "assistant"

    @Column(length = 4000)
    private String content;

    private LocalDateTime createdAt = LocalDateTime.now();

    public ChatMessage(AppUser user, String role, String content) {
        this.user = user;
        this.role = role;
        this.content = content;
    }
}
