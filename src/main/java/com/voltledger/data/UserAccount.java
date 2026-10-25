package com.voltledger.data;

/** Domain account: holds only a BCrypt hash; neither password nor Firebase keys are persisted. */
public record UserAccount(String username, String displayName, String passwordHash,
                          String role, boolean enabled) { }
