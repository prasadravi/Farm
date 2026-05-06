package com.naturalmilk.service;

import com.naturalmilk.model.ContactMessage;
import org.springframework.stereotype.Service;

@Service
public class ContactService {
    public ContactMessage createContactMessage(ContactMessage payload) {
        return payload;
    }
}
