package com.voltledger.web;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class NavModelAdvice {
    private final Environment environment;
    public NavModelAdvice(Environment environment) { this.environment = environment; }

    @ModelAttribute("isPreview")
    public boolean preview() { return environment.acceptsProfiles(Profiles.of("preview")); }

    @ModelAttribute("signedIn")
    public boolean signedIn() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);
    }
}
