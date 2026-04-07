package com.naturalmilk.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
public class FirebaseConfig {

    @Value("${firebase.credentials}")
    private String firebaseCredentials;

    @Value("${firebase.credentials.json:}")
    private String firebaseCredentialsJson;

    @Value("${firebase.database-url}")
    private String firebaseDatabaseUrl;

    @Bean
    public Firestore firestore() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            GoogleCredentials credentials;

            if (firebaseCredentialsJson != null && !firebaseCredentialsJson.isBlank()) {
                ByteArrayInputStream serviceAccount = new ByteArrayInputStream(
                        firebaseCredentialsJson.getBytes(StandardCharsets.UTF_8));
                credentials = GoogleCredentials.fromStream(serviceAccount);
            } else {
                FileInputStream serviceAccount = new FileInputStream(firebaseCredentials);
                credentials = GoogleCredentials.fromStream(serviceAccount);
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .setDatabaseUrl(firebaseDatabaseUrl)
                    .build();

            FirebaseApp.initializeApp(options);
            System.out.println("Firebase initialized successfully");
        }

        return FirestoreClient.getFirestore();
    }
}
