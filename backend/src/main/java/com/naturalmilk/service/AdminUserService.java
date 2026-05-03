package com.naturalmilk.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.naturalmilk.model.AdminUser;
import com.naturalmilk.repository.AdminUserRepository;

@Service
public class AdminUserService {
    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserService(AdminUserRepository adminUserRepository, PasswordEncoder passwordEncoder) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AdminUser ensureDefaultAdmin(String username, String rawPassword) {
        if (adminUserRepository.count() > 0) {
            return adminUserRepository.findFirstByOrderByCreatedAtAsc().orElse(null);
        }
        AdminUser admin = new AdminUser();
        long now = System.currentTimeMillis();
        admin.setCreatedAt(now);
        admin.setUpdatedAt(now);
        admin.setUsername(normalizeUsername(username));
        admin.setPasswordHash(passwordEncoder.encode(normalizePassword(rawPassword)));
        return adminUserRepository.save(admin);
    }

    public AdminUser getPrimaryAdmin() {
        return adminUserRepository.findFirstByOrderByCreatedAtAsc().orElse(null);
    }

    public AdminUser updateCredentials(String username, String rawPassword) {
        AdminUser admin = getPrimaryAdmin();
        long now = System.currentTimeMillis();
        if (admin == null) {
            admin = new AdminUser();
            admin.setCreatedAt(now);
            admin.setPasswordHash(passwordEncoder.encode(normalizePassword(rawPassword)));
        } else if (rawPassword != null && !rawPassword.isEmpty()) {
            admin.setPasswordHash(passwordEncoder.encode(rawPassword));
        }
        admin.setUsername(normalizeUsername(username));
        admin.setUpdatedAt(now);
        return adminUserRepository.save(admin);
    }

    private String normalizeUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return "admin";
        }
        return username.trim();
    }

    private String normalizePassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            return "admin123";
        }
        return rawPassword;
    }
}
