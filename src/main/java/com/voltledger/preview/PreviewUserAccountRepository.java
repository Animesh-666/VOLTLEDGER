package com.voltledger.preview;

import com.voltledger.data.UserAccount;
import com.voltledger.data.UserAccountRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

/** Local-only, volatile store. No data or password hash survives an application restart. */
@Repository
@Profile("preview")
public class PreviewUserAccountRepository implements UserAccountRepository {
    private final Map<String, UserAccount> accounts = new ConcurrentHashMap<>();

    @Override public Optional<UserAccount> findByUsername(String username) {
        return Optional.ofNullable(accounts.get(username));
    }

    @Override public void create(String username, String displayName, String passwordHash, String role) {
        if (!"CONSUMER".equals(role) && !"ADMIN".equals(role))
            throw new IllegalArgumentException("Invalid role");
        UserAccount account = new UserAccount(username, displayName, passwordHash, role, true);
        if (accounts.putIfAbsent(username, account) != null)
            throw new DuplicateKeyException("Username already registered");
    }

    @Override public List<UserAccount> consumers() {
        return accounts.values().stream().filter(a -> a.enabled() && "CONSUMER".equals(a.role()))
            .sorted(Comparator.comparing(UserAccount::username)).toList();
    }

    @Override public int adminCount() {
        return (int) accounts.values().stream().filter(a -> "ADMIN".equals(a.role())).count();
    }
}
