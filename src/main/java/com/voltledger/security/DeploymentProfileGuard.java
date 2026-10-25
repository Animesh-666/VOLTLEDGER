package com.voltledger.security;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

/** Fail closed on Render: never silently use temporary preview persistence in a live service. */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public final class DeploymentProfileGuard implements ApplicationRunner {
    private final Environment environment;

    public DeploymentProfileGuard(Environment environment) { this.environment = environment; }

    @Override
    public void run(ApplicationArguments args) {
        if (!"true".equalsIgnoreCase(environment.getProperty("RENDER", "false"))) return;
        if (!environment.acceptsProfiles(Profiles.of("firebase")) ||
            !environment.acceptsProfiles(Profiles.of("production")) ||
            environment.acceptsProfiles(Profiles.of("preview"))) {
            throw new IllegalStateException(
                "Render requires SPRING_PROFILES_ACTIVE=firebase,production; preview is forbidden");
        }
    }
}
