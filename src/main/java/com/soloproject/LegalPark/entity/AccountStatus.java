package com.soloproject.LegalPark.entity;

public enum AccountStatus {
    PENDING_VERIFICATION, // New account registered, awaiting email verification
    ACTIVE,               // Active account, verified email, payment feature enabled
    INACTIVE,             // Account deactivated (e.g., due to logout or admin)
    SUSPENDED,            // Account suspended
    BLOCKED               // Account blocked
}
