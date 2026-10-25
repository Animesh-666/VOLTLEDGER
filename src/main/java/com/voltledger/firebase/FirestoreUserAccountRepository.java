package com.voltledger.firebase;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.voltledger.data.UserAccount;
import com.voltledger.data.UserAccountRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

/** Firestore identities continue to be authenticated by Spring Security + BCrypt. */
@Repository
@Profile("firebase")
public class FirestoreUserAccountRepository implements UserAccountRepository {
    private final Firestore db;
    public FirestoreUserAccountRepository(Firestore db) { this.db = db; }

    @Override public Optional<UserAccount> findByUsername(String username) {
        if (username == null || username.isBlank()) return Optional.empty();
        return Optional.ofNullable(from(FirestoreCalls.await(db.collection("users").document(username).get())));
    }

    @Override public void create(String username, String name, String hash, String role) {
        if (!"CONSUMER".equals(role) && !"ADMIN".equals(role))
            throw new IllegalArgumentException("Invalid role");
        FirestoreCalls.await(db.collection("users").document(username).create(
            java.util.Map.of("username", username, "displayName", name, "passwordHash", hash,
                "role", role, "enabled", true)));
    }

    @Override public List<UserAccount> consumers() {
        return FirestoreCalls.await(db.collection("users")
            .whereEqualTo("role", "CONSUMER").get()).getDocuments().stream()
            .map(FirestoreUserAccountRepository::from).filter(a -> a != null && a.enabled())
            .sorted(java.util.Comparator.comparing(UserAccount::username)).toList();
    }

    @Override public int adminCount() {
        return (int) FirestoreCalls.await(db.collection("users")
            .whereEqualTo("role", "ADMIN").get()).size();
    }

    private static UserAccount from(DocumentSnapshot d) {
        if (!d.exists()) return null;
        return new UserAccount(d.getId(), d.getString("displayName"), d.getString("passwordHash"),
            d.getString("role"), Boolean.TRUE.equals(d.getBoolean("enabled")));
    }
}
