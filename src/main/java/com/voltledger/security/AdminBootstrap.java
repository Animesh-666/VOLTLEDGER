package com.voltledger.security;

import com.voltledger.data.UserAccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Bootstrap is opt-in via a strong environment secret and never promotes a consumer. */
@Component
@Order(10)
public class AdminBootstrap implements ApplicationRunner {
    private final UserAccountRepository users;
    private final PasswordEncoder encoder;
    private final String username;
    private final String password;
    private final Environment environment;

    public AdminBootstrap(UserAccountRepository users, PasswordEncoder encoder,
        @Value("${voltledger.bootstrap.admin.username:admin}") String username,
        @Value("${voltledger.bootstrap.admin.password:}") String password, Environment environment) {
        this.users=users; this.encoder=encoder;
        this.username=username; this.password=password; this.environment=environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (users.adminCount() > 0) return;
        // The default preview opens without any secrets; privileged demo features are opt-in.
        if (environment.acceptsProfiles(Profiles.of("preview")) && password.isBlank()) return;
        if (!username.matches("[a-z][a-z0-9_]{2,29}") || password.length() < 12 ||
            password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            throw new IllegalStateException("First launch requires VL_BOOTSTRAP_ADMIN_USERNAME and " +
                "VL_BOOTSTRAP_ADMIN_PASSWORD (12-72 UTF-8 bytes)");
        }
        if (users.findByUsername(username).isPresent()) {
            throw new IllegalStateException("Bootstrap admin username already belongs to an account; use another");
        }
        users.create(username, "VOLTLEDGER Administrator", encoder.encode(password), "ADMIN");
    }
}
