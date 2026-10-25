package com.voltledger.preview;

import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("preview")
public class PreviewHealthController {
    @GetMapping("/api/health")
    public Map<String,String> health() {
        return Map.of("status", "UP", "application", "VOLTLEDGER", "storage", "VOLATILE_PREVIEW");
    }
}
