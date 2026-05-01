package com.naturalmilk.service;

import com.naturalmilk.model.ContactMessage;
import org.springframework.stereotype.Service;

@Service
public class ContactService {
    // TODO: Inject repository and implement DB logic for PostgreSQL

    public ContactMessage createContactMessage(ContactMessage payload) {
        // This method will be implemented after ContactMessage is updated for JPA/PostgreSQL
        // For now, just return the payload
        return payload;
    }
}
