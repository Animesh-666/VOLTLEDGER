package com.voltledger.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
            .requestMatchers("/", "/project", "/calculator", "/login", "/register", "/api/health",
                "/api/live", "/error", "/css/**", "/js/**", "/assets/**", "/favicon.ico").permitAll()
            .requestMatchers("/admin", "/admin/**").hasRole("ADMIN")
            .requestMatchers("/account", "/account/**").hasRole("CONSUMER")
            .anyRequest().authenticated())
            .formLogin(form -> form.loginPage("/login").defaultSuccessUrl("/dashboard", true).permitAll())
            .logout(logout -> logout.logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true).clearAuthentication(true)
                .deleteCookies("JSESSIONID").permitAll())
            .sessionManagement(session -> session.sessionFixation(fixation -> fixation.migrateSession()))
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp.policyDirectives(
                    "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; " +
                    "img-src 'self' data:; font-src 'self'; connect-src 'self'; object-src 'none'; " +
                    "base-uri 'self'; form-action 'self'; frame-ancestors 'none'")));
        // Spring Security defaults retain CSRF protection and frame-deny / nosniff / HSTS headers.
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
}
