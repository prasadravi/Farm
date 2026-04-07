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
import java.util.Base64;

@Configuration
public class FirebaseConfig {

    @Value("${firebase.credentials}")
    private String firebaseCredentials;

    @Value("${firebase.credentials.json:}")
    private String firebaseCredentialsJson;

    @Value("${firebase.credentials.base64:}")
    private String firebaseCredentialsBase64;

    @Value("${firebase.database-url}")
    private String firebaseDatabaseUrl;

    @Bean
    public Firestore firestore() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            GoogleCredentials credentials;

            if (firebaseCredentialsBase64 != null && !firebaseCredentialsBase64.isBlank()) {
                try {
                    byte[] decoded = Base64.getDecoder().decode(firebaseCredentialsBase64.trim());
                    credentials = GoogleCredentials.fromStream(new ByteArrayInputStream(decoded));
                } catch (IllegalArgumentException ex) {
                    throw new IOException("Invalid FIREBASE_CREDENTIALS_BASE64 value. Please provide valid base64-encoded service account JSON.", ex);
                }
            } else if (firebaseCredentialsJson != null && !firebaseCredentialsJson.isBlank()) {
                String normalizedJson = firebaseCredentialsJson.trim();

                // Render users sometimes paste JSON wrapped in single/double quotes.
                if ((normalizedJson.startsWith("\"") && normalizedJson.endsWith("\""))
                        || (normalizedJson.startsWith("'") && normalizedJson.endsWith("'"))) {
                    normalizedJson = normalizedJson.substring(1, normalizedJson.length() - 1);
                }

                ByteArrayInputStream serviceAccount = new ByteArrayInputStream(
                        normalizedJson.getBytes(StandardCharsets.UTF_8));
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
