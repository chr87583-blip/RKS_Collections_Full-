package com.rks.collections.controller;

import com.rks.collections.dto.AuthDto;
import com.rks.collections.model.AppUser;
import com.rks.collections.repository.NotificationRepository;
import com.rks.collections.security.JwtUtil;
import com.rks.collections.service.NotificationService;
import com.rks.collections.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private AuthenticationManager authManager;
    @Autowired private UserService userService;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private NotificationService notifService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthDto.LoginRequest req) {
        try {
            authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid username or password"));
        }

        UserDetails ud = userService.loadUserByUsername(req.getUsername());
        String token = jwtUtil.generateToken(ud);
        userService.updateLastLogin(req.getUsername());

        AppUser user = userService.findByUsername(req.getUsername()).orElseThrow();
        long unread = notifService.countUnread(req.getUsername());

        return ResponseEntity.ok(new AuthDto.AuthResponse(
            token, user.getUsername(),
            user.getRole().name(),
            user.getFullName(),
            unread
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody AuthDto.RegisterRequest req) {
        try {
            AppUser user = userService.register(req);
            // Send welcome notification
            notifService.sendWelcome(user.getUsername());

            UserDetails ud = userService.loadUserByUsername(user.getUsername());
            String token = jwtUtil.generateToken(ud);

            return ResponseEntity.ok(new AuthDto.AuthResponse(
                token, user.getUsername(), user.getRole().name(), user.getFullName(), 1
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/fcm-token")
    public ResponseEntity<?> saveFcmToken(@RequestBody Map<String, String> body,
                                           @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String username = jwtUtil.extractUsername(token);
        userService.saveFcmToken(username, body.get("fcmToken"));
        return ResponseEntity.ok(Map.of("status", "FCM token saved"));
    }
}
