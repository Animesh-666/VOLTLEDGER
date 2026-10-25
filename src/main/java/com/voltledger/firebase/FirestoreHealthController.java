package com.voltledger.firebase;

import com.google.cloud.firestore.Firestore;
import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Public readiness endpoint intentionally never returns service-account details. */
@RestController
@Profile("firebase")
public class FirestoreHealthController {
    private final Firestore db;
    private static final long CACHE_NANOS = java.util.concurrent.TimeUnit.SECONDS.toNanos(60);
    private volatile long lastCheckNanos;
    private volatile boolean healthy;
    public FirestoreHealthController(Firestore db) { this.db = db; }

    @GetMapping("/api/health")
    public ResponseEntity<Map<String,String>> health() {
        refreshIfStale();
        return healthy
            ? ResponseEntity.ok(Map.of("status", "UP", "application", "VOLTLEDGER"))
            : ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("status", "UNAVAILABLE", "application", "VOLTLEDGER"));
    }

    // Avoid a billable Firestore read on every health check or refresh.
    private synchronized void refreshIfStale() {
        long now = System.nanoTime();
        if (lastCheckNanos != 0 && now - lastCheckNanos < CACHE_NANOS) return;
        try {
            FirestoreCalls.await(db.collection("_health").document("connectivity-check").get());
            healthy = true;
        } catch (RuntimeException ex) {
            healthy = false;
        }
        lastCheckNanos = System.nanoTime();
    }
}
