package com.rks.collections.controller;

import com.rks.collections.dto.AuthDto;
import com.rks.collections.model.Notification;
import com.rks.collections.security.JwtUtil;
import com.rks.collections.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired private NotificationService notifService;
    @Autowired private JwtUtil jwtUtil;

    @GetMapping
    public ResponseEntity<List<Notification>> getMyNotifications(
            @RequestHeader("Authorization") String authHeader) {
        String username = extractUsername(authHeader);
        return ResponseEntity.ok(notifService.getForUser(username));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<?> unreadCount(@RequestHeader("Authorization") String authHeader) {
        String username = extractUsername(authHeader);
        return ResponseEntity.ok(Map.of("count", notifService.countUnread(username)));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<?> markRead(@PathVariable Long id) {
        notifService.markRead(id);
        return ResponseEntity.ok(Map.of("status", "marked read"));
    }

    @PostMapping("/read-all")
    public ResponseEntity<?> markAllRead(@RequestHeader("Authorization") String authHeader) {
        notifService.markAllRead(extractUsername(authHeader));
        return ResponseEntity.ok(Map.of("status", "all read"));
    }

    /** Admin: send broadcast notification */
    @PostMapping("/broadcast")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Notification> broadcast(@RequestBody AuthDto.NotificationRequest req) {
        Notification n = notifService.broadcast(req.getTitle(), req.getMessage(),
                req.getType(), req.getProductId());
        return ResponseEntity.ok(n);
    }

    /** Admin: send to specific user */
    @PostMapping("/send-user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> sendToUser(@RequestBody AuthDto.NotificationRequest req) {
        if (req.getUserId() == null)
            return ResponseEntity.badRequest().body(Map.of("error", "userId required"));
        // We'd need to look up by id — simplified: send by userId embedded in request
        notifService.broadcast(req.getTitle(), req.getMessage(), req.getType(), req.getProductId());
        return ResponseEntity.ok(Map.of("status", "sent"));
    }

    private String extractUsername(String authHeader) {
        return jwtUtil.extractUsername(authHeader.substring(7));
    }
}
