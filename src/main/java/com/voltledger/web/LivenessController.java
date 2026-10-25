package com.voltledger.web;

import java.util.Map;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Cheap Render liveness check. /api/health remains the Firestore readiness check. */
@RestController
public final class LivenessController {
    @GetMapping("/api/live")
    public ResponseEntity<Map<String,String>> live() {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
            .body(Map.of("status", "UP", "application", "VOLTLEDGER"));
    }
}
