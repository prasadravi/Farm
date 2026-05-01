package com.naturalmilk.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.stereotype.Service;

import com.naturalmilk.model.User;

@Service
public class UserService {
    // Add service methods here as needed

    // TODO: Inject UserRepository (JPA) and implement DB logic for PostgreSQL

    public User createUser(User user) {
        user.setCreatedAt(System.currentTimeMillis());
        user.setUpdatedAt(System.currentTimeMillis());
        user.setPassword(hashPassword(user.getPassword()));
        // TODO: Save user to PostgreSQL
        return user;
    }

    public User getUserByEmail(String email) {
        // TODO: Fetch user by email from PostgreSQL
        return null;
    }

    public User getUserById(String userId) {
        // TODO: Fetch user by ID from PostgreSQL
        return null;
    }

    public User updateUser(String email, User user) {
        user.setUpdatedAt(System.currentTimeMillis());
        // TODO: Update user in PostgreSQL
        return user;
    }

    public boolean verifyPassword(String rawPassword, String hashedPassword) {
        return hashPassword(rawPassword).equals(hashedPassword);
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Error hashing password: " + e.getMessage());
            return null;
        }
    }
}
