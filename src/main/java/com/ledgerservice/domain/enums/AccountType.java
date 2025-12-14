package com.ledgerservice.domain.enums;

/**
 * Type of account in the ledger system
 */
public enum AccountType {
    /**
     * End-user accounts
     */
    USER,

    /**
     * Internal system accounts (e.g., revenue, fees)
     */
    SYSTEM,

    /**
     * Temporary transit accounts for multi-step operations
     */
    TRANSIT
}