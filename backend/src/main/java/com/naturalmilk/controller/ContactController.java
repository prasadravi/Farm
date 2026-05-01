package com.naturalmilk.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.naturalmilk.model.ContactMessage;
import com.naturalmilk.service.ContactService;


@RestController
@RequestMapping("/contact")
public class ContactController {

    @Autowired
    private ContactService contactService;

    @PostMapping
    public ResponseEntity<?> submitContactMessage(@RequestBody ContactMessage payload) {
        try {
            if (payload.getFirstName() == null || payload.getFirstName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("First name is required.");
            }
            if (payload.getEmail() == null || payload.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Email is required.");
            }
            if (payload.getMessage() == null || payload.getMessage().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Message is required.");
            }

            ContactMessage saved = contactService.createContactMessage(payload);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to submit contact message: " + e.getMessage());
        }
    }
}
