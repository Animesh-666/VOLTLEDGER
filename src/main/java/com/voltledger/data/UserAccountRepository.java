package com.voltledger.data;

import java.util.List;
import java.util.Optional;

/** Storage contract implemented by in-memory preview and Firebase Firestore adapters. */
public interface UserAccountRepository {
    Optional<UserAccount> findByUsername(String username);
    void create(String username, String name, String hash, String role);
    List<UserAccount> consumers();
    int adminCount();
}
