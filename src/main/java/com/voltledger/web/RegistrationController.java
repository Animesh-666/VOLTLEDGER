package com.voltledger.web;

import com.voltledger.data.UserAccountRepository;
import jakarta.validation.Valid;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RegistrationController {
    private final UserAccountRepository users;
    private final PasswordEncoder encoder;
    private final boolean registrationEnabled;
    public RegistrationController(UserAccountRepository users, PasswordEncoder encoder,
        @Value("${voltledger.registration.enabled:true}") boolean registrationEnabled) {
        this.users=users; this.encoder=encoder; this.registrationEnabled=registrationEnabled;
    }
    private void requireRegistrationEnabled() {
        if (!registrationEnabled) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/register")
    public String register(Model model) {
        requireRegistrationEnabled();
        if (!model.containsAttribute("registrationForm")) model.addAttribute("registrationForm", new RegistrationForm());
        return "register";
    }

    @PostMapping("/register")
    public String create(@Valid @ModelAttribute("registrationForm") RegistrationForm form,
        BindingResult errors, RedirectAttributes redirect) {
        requireRegistrationEnabled();
        if (!form.getPassword().equals(form.getConfirmPassword()))
            errors.rejectValue("confirmPassword", "mismatch", "Passwords must match");
        if (form.getPassword().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72)
            errors.rejectValue("password", "tooLong", "Use a password of 72 UTF-8 bytes or fewer");
        if (errors.hasErrors()) return clearPassword(form);
        try {
            // The requested role is intentionally NOT an input: registration only creates consumers.
            users.create(form.getUsername(), form.getDisplayName().trim(),
                encoder.encode(form.getPassword()), "CONSUMER");
        } catch (DuplicateKeyException ex) {
            errors.rejectValue("username", "duplicate", "This username is already taken");
            return clearPassword(form);
        }
        redirect.addFlashAttribute("registered", "Account created. Please sign in.");
        return "redirect:/login";
    }

    private String clearPassword(RegistrationForm form) {
        // Do not echo a submitted password back in the rendered HTML after validation fails.
        form.setPassword("");
        form.setConfirmPassword("");
        return "register";
    }
}
