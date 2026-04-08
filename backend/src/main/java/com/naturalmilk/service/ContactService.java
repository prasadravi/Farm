package com.naturalmilk.service;

import com.google.cloud.firestore.Firestore;
import com.naturalmilk.model.ContactMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
public class ContactService {

    @Autowired
    private Firestore firestore;

    public ContactMessage createContactMessage(ContactMessage payload) throws ExecutionException, InterruptedException {
        ContactMessage message = new ContactMessage();
        message.setId(UUID.randomUUID().toString());
        message.setFirstName(payload.getFirstName());
        message.setLastName(payload.getLastName());
        message.setEmail(payload.getEmail());
        message.setSubject(payload.getSubject());
        message.setMessage(payload.getMessage());
        message.setCreatedAt(System.currentTimeMillis());

        firestore.collection("contactMessages").document(message.getId()).set(message).get();
        return message;
    }
}
