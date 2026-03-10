package com.rks.collections.service;

import com.rks.collections.model.AppUser;
import com.rks.collections.model.Notification;
import com.rks.collections.repository.NotificationRepository;
import com.rks.collections.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationService {

    @Autowired private NotificationRepository notifRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private SimpMessagingTemplate messagingTemplate;

    /** Broadcast to ALL users via WebSocket + save to DB */
    public Notification broadcast(String title, String message, String type, String productId) {
        Notification n = new Notification(title, message, type);
        n.setProductId(productId);
        n = notifRepo.save(n);

        // Push via WebSocket to all connected clients
        Map<String, Object> payload = buildPayload(n);
        messagingTemplate.convertAndSend("/topic/notifications", payload);

        return n;
    }

    /** Send to a specific user */
    public Notification sendToUser(String username, String title, String message, String type) {
        AppUser user = userRepo.findByUsername(username).orElseThrow();
        Notification n = new Notification(title, message, type, user);
        n = notifRepo.save(n);

        // Push to specific user via WebSocket
        Map<String, Object> payload = buildPayload(n);
        messagingTemplate.convertAndSendToUser(username, "/queue/notifications", payload);

        return n;
    }

    public List<Notification> getForUser(String username) {
        AppUser user = userRepo.findByUsername(username).orElseThrow();
        return notifRepo.findForUser(user);
    }

    public long countUnread(String username) {
        AppUser user = userRepo.findByUsername(username).orElse(null);
        if (user == null) return 0;
        return notifRepo.countUnreadForUser(user);
    }

    public void markRead(Long notifId) {
        notifRepo.findById(notifId).ifPresent(n -> {
            n.setRead(true);
            notifRepo.save(n);
        });
    }

    public void markAllRead(String username) {
        AppUser user = userRepo.findByUsername(username).orElseThrow();
        List<Notification> notifs = notifRepo.findForUser(user);
        notifs.forEach(n -> n.setRead(true));
        notifRepo.saveAll(notifs);
    }

    private Map<String, Object> buildPayload(Notification n) {
        Map<String, Object> p = new HashMap<>();
        p.put("id", n.getId());
        p.put("title", n.getTitle());
        p.put("message", n.getMessage());
        p.put("type", n.getType());
        p.put("productId", n.getProductId());
        p.put("createdAt", n.getCreatedAt().toString());
        return p;
    }

    // Seed welcome notification
    public void sendWelcome(String username) {
        sendToUser(username, "Welcome to RKS Collections! 🎉",
            "Thank you for joining. Explore our exclusive ethnic wear collection.", "WELCOME");
    }
}
