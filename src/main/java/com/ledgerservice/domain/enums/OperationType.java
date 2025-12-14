package com.ledgerservice.domain.enums;

/**
 * Type of financial operation
 */
public enum OperationType {
    /**
     * Money entering an account
     */
    DEPOSIT,

    /**
     * Money leaving an account
     */
    WITHDRAWAL,

    /**
     * Money moving between two accounts
     */
    TRANSFER
}
