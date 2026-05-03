package com.naturalmilk.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.naturalmilk.model.AuthRequest;
import com.naturalmilk.model.AuthResponse;
import com.naturalmilk.model.User;
import com.naturalmilk.security.JwtTokenProvider;
import com.naturalmilk.service.UserService;

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
                    new AuthResponse(null, null, "Email already registered")
                );
            }

            User user = new User();
            user.setName(request.getName());
            user.setEmail(request.getEmail());
            user.setPassword(request.getPassword());

            User createdUser = userService.createUser(user);
            String token = jwtTokenProvider.generateToken(createdUser.getEmail());

            return ResponseEntity.ok(
                new AuthResponse(token, createdUser, "Registration successful")
            );
        } catch (Exception e) {
            System.err.println("Registration error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(
                new AuthResponse(null, null, "Registration failed: " + e.getMessage())
            );
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            User user = userService.getUserByEmail(request.getEmail());
            if (user == null) {
                return ResponseEntity.badRequest().body(
                    new AuthResponse(null, null, "Invalid email or password")
                );
            }

            if (!userService.verifyPassword(request.getPassword(), user.getPassword())) {
                return ResponseEntity.badRequest().body(
                    new AuthResponse(null, null, "Invalid email or password")
                );
            }

            String token = jwtTokenProvider.generateToken(user.getEmail());
            user.setPassword(null); // Don't send password in response

            return ResponseEntity.ok(
                new AuthResponse(token, user, "Login successful")
            );
        } catch (Exception e) {
            System.err.println("Login error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(
                new AuthResponse(null, null, "Login failed: " + e.getMessage())
            );
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            if (token == null || token.isBlank() || !token.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body("Unauthorized");
            }
            String userId = jwtTokenProvider.getUserIdFromToken(token.replace("Bearer ", ""));
            User user = userService.getUserByEmail(userId);
            if (user != null) {
                user.setPassword(null);
            }
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Unauthorized");
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
            if (updateData.getLandmark() != null) user.setLandmark(updateData.getLandmark());
            if (updateData.getPincode() != null) user.setPincode(updateData.getPincode());

            User updated = userService.updateUser(email, user);
            updated.setPassword(null);
            
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            System.err.println("Update profile error: " + e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to update profile");
        }
    }
}
