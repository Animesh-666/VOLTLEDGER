package com.voltledger.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import static org.junit.jupiter.api.Assertions.*;

class DeploymentProfileGuardTest {
    @Test void blocksPreviewOnRender() {
        var environment = new MockEnvironment().withProperty("RENDER", "true");
        environment.setActiveProfiles("preview");
        assertThrows(IllegalStateException.class, () -> new DeploymentProfileGuard(environment).run(null));
    }

    @Test void requiresBothFirebaseAndProductionOnRender() {
        var environment = new MockEnvironment().withProperty("RENDER", "true");
        environment.setActiveProfiles("firebase");
        assertThrows(IllegalStateException.class, () -> new DeploymentProfileGuard(environment).run(null));
    }

    @Test void acceptsFirebaseProductionOnRender() {
        var environment = new MockEnvironment().withProperty("RENDER", "true");
        environment.setActiveProfiles("firebase", "production");
        assertDoesNotThrow(() -> new DeploymentProfileGuard(environment).run(null));
    }

    @Test void preservesLocalPreview() {
        var environment = new MockEnvironment();
        environment.setActiveProfiles("preview");
        assertDoesNotThrow(() -> new DeploymentProfileGuard(environment).run(null));
    }
}
