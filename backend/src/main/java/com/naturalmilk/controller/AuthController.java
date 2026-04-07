package com.naturalmilk.controller;

import com.naturalmilk.model.*;
import com.naturalmilk.security.JwtTokenProvider;
import com.naturalmilk.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthRequest request) {
        try {
            User existingUser = userService.getUserByEmail(request.getEmail());
            if (existingUser != null) {
                return ResponseEntity.badRequest().body(
                    AuthResponse.builder()
                        .message("Email already registered")
                        .build()
                );
            }

            User user = User.builder()
                    .name(request.getName())
                    .email(request.getEmail())
                    .password(request.getPassword())
                    .build();

            User createdUser = userService.createUser(user);
            String token = jwtTokenProvider.generateToken(createdUser.getEmail());

            return ResponseEntity.ok(
                AuthResponse.builder()
                    .token(token)
                    .user(createdUser)
                    .message("Registration successful")
                    .build()
            );
        } catch (Exception e) {
            System.err.println("Registration error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(
                AuthResponse.builder()
                    .message("Registration failed: " + e.getMessage())
                    .build()
            );
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            User user = userService.getUserByEmail(request.getEmail());
            if (user == null) {
                return ResponseEntity.badRequest().body(
                    AuthResponse.builder()
                        .message("Invalid email or password")
                        .build()
                );
            }

            if (!userService.verifyPassword(request.getPassword(), user.getPassword())) {
                return ResponseEntity.badRequest().body(
                    AuthResponse.builder()
                        .message("Invalid email or password")
                        .build()
                );
            }

            String token = jwtTokenProvider.generateToken(user.getEmail());
            user.setPassword(null); // Don't send password in response

            return ResponseEntity.ok(
                AuthResponse.builder()
                    .token(token)
                    .user(user)
                    .message("Login successful")
                    .build()
            );
        } catch (Exception e) {
            System.err.println("Login error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(
                AuthResponse.builder()
                    .message("Login failed: " + e.getMessage())
                    .build()
            );
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String token) {
        try {
            String userId = jwtTokenProvider.getUserIdFromToken(token.replace("Bearer ", ""));
            User user = userService.getUserByEmail(userId);
            if (user != null) {
                user.setPassword(null);
            }
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            System.err.println("Get user error: " + e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to get user");
        }
    }

    @PutMapping("/update-profile")
    public ResponseEntity<?> updateProfile(@RequestHeader("Authorization") String token, @RequestBody User updateData) {
        try {
            String email = jwtTokenProvider.getUserIdFromToken(token.replace("Bearer ", ""));
            User user = userService.getUserByEmail(email);
            
            if (updateData.getName() != null) user.setName(updateData.getName());
            if (updateData.getPhone() != null) user.setPhone(updateData.getPhone());
            if (updateData.getAddress() != null) user.setAddress(updateData.getAddress());

            User updated = userService.updateUser(email, user);
            updated.setPassword(null);
            
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            System.err.println("Update profile error: " + e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to update profile");
        }
    }
}
