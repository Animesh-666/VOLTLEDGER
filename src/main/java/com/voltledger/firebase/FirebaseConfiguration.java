package com.voltledger.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/** Backend-only Firestore initialization. Never ship the service key to the browser. */
@Configuration
@Profile("firebase")
public class FirebaseConfiguration {
    @Bean(destroyMethod = "close")
    public Firestore firestore(@Value("${voltledger.firebase.project-id:}") String configuredProject)
            throws IOException {
        String encoded = System.getenv("FIREBASE_SERVICE_ACCOUNT_JSON_BASE64");
        GoogleCredentials credentials;
        if (encoded != null && !encoded.isBlank()) {
            try {
                credentials = GoogleCredentials.fromStream(new ByteArrayInputStream(
                    Base64.getDecoder().decode(encoded.trim())));
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException("Invalid base64 Firebase service account environment variable", e);
            }
        } else {
            // Local Windows: set GOOGLE_APPLICATION_CREDENTIALS to the private JSON key's path.
            credentials = GoogleCredentials.getApplicationDefault();
        }
        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseOptions.Builder options = FirebaseOptions.builder().setCredentials(credentials);
            if (!configuredProject.isBlank()) options.setProjectId(configuredProject);
            FirebaseApp.initializeApp(options.build());
        }
        return FirestoreClient.getFirestore();
    }
}
