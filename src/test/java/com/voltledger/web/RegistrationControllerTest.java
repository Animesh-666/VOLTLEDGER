package com.voltledger.web;

import com.voltledger.data.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.ui.ExtendedModelMap;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RegistrationControllerTest {
    @Test void productionCanDisablePublicRegistration() {
        var controller = new RegistrationController(mock(UserAccountRepository.class),
            new BCryptPasswordEncoder(), false);
        assertThrows(ResponseStatusException.class,
            () -> controller.register(new ExtendedModelMap()));
    }

    @Test void previewCanDisplayRegistration() {
        var controller = new RegistrationController(mock(UserAccountRepository.class),
            new BCryptPasswordEncoder(), true);
        assertEquals("register", controller.register(new ExtendedModelMap()));
    }
}
