package com.voltledger.security;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

/** Rejects accidental deployment using volatile preview storage. */
@Component
@Profile("production")
@Order(1)
public class ProductionProfileGuard implements ApplicationRunner {
    private final Environment environment;
    public ProductionProfileGuard(Environment environment) { this.environment = environment; }
    @Override public void run(ApplicationArguments arguments) {
        if (!environment.acceptsProfiles(Profiles.of("firebase")) ||
            environment.acceptsProfiles(Profiles.of("preview"))) {
            throw new IllegalStateException("Production requires firebase profile and cannot use preview: " +
                "SPRING_PROFILES_ACTIVE=firebase,production");
        }
    }
}
