package com.rks.collections.service;

import com.rks.collections.dto.AuthDto;
import com.rks.collections.model.AppUser;
import com.rks.collections.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username}") private String adminUsername;
    @Value("${app.admin.password}") private String adminPassword;
    @Value("${app.admin.email}")    private String adminEmail;

    // Constructor injection — Spring resolves PasswordEncoder from PasswordConfig, no cycle
    public UserService(UserRepository userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void createDefaultAdmin() {
        if (!userRepo.existsByUsername(adminUsername)) {
            AppUser admin = new AppUser();
            admin.setUsername(adminUsername);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setEmail(adminEmail);
            admin.setFullName("RKS Admin");
            admin.setRole(AppUser.Role.ADMIN);
            userRepo.save(admin);
            System.out.println("✅ Admin created — login: " + adminUsername + " / " + adminPassword);
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser user = userRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }

    public AppUser register(AuthDto.RegisterRequest req) {
        if (userRepo.existsByUsername(req.getUsername()))
            throw new RuntimeException("Username already taken");
        if (userRepo.existsByEmail(req.getEmail()))
            throw new RuntimeException("Email already registered");
        AppUser user = new AppUser();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setEmail(req.getEmail());
        user.setFullName(req.getFullName());
        user.setPhone(req.getPhone());
        user.setRole(AppUser.Role.USER);
        return userRepo.save(user);
    }

    public Optional<AppUser> findByUsername(String username) { return userRepo.findByUsername(username); }

    public void updateLastLogin(String username) {
        userRepo.findByUsername(username).ifPresent(u -> {
            u.setLastLogin(LocalDateTime.now());
            userRepo.save(u);
        });
    }

    public void saveFcmToken(String username, String token) {
        userRepo.findByUsername(username).ifPresent(u -> { u.setFcmToken(token); userRepo.save(u); });
    }

    public List<AppUser> getAllUsers() { return userRepo.findAll(); }
}
