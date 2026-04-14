package com.naturalmilk.service;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.naturalmilk.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.util.concurrent.ExecutionException;

@Service
public class UserService {

    @Autowired
    private Firestore firestore;

    public User createUser(User user) throws ExecutionException, InterruptedException {
        user.setCreatedAt(System.currentTimeMillis());
        user.setUpdatedAt(System.currentTimeMillis());
        user.setPassword(hashPassword(user.getPassword()));

        firestore.collection("users").document(user.getEmail()).set(user).get();
        return user;
    }

    public User getUserByEmail(String email) throws ExecutionException, InterruptedException {
        return firestore.collection("users").document(email).get().get().toObject(User.class);
    }

    public User getUserById(String userId) throws ExecutionException, InterruptedException {
        QuerySnapshot query = firestore.collection("users")
                .whereEqualTo("id", userId)
                .get()
                .get();

        if (query.getDocuments().isEmpty()) {
            return null;
        }
        return query.getDocuments().get(0).toObject(User.class);
    }

    public User updateUser(String email, User user) throws ExecutionException, InterruptedException {
        user.setUpdatedAt(System.currentTimeMillis());
        firestore.collection("users").document(email).set(user).get();
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
        } catch (Exception e) {
            System.err.println("Error hashing password: " + e.getMessage());
            return null;
        }
    }
}
