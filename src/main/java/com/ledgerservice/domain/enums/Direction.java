package com.ledgerservice.domain.enums;

/**
 * Direction of a ledger entry (double-entry bookkeeping)
 */
public enum Direction {
    /**
     * Money entering an account (positive impact on balance)
     */
    CREDIT,

    /**
     * Money leaving an account (negative impact on balance)
     */
    DEBIT
}