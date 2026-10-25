package com.voltledger.security;

import com.voltledger.data.UserAccountRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {
    private final UserAccountRepository users;
    public DatabaseUserDetailsService(UserAccountRepository users) { this.users = users; }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var account = users.findByUsername(username.trim().toLowerCase(java.util.Locale.ROOT))
            .orElseThrow(() -> new UsernameNotFoundException("Account not found"));
        return User.withUsername(account.username())
            .password(account.passwordHash())
            .authorities("ROLE_" + account.role())
            .disabled(!account.enabled())
            .build();
    }
}
