package com.ledgerservice.domain.enums;

/**
 * Result of a reconciliation attempt
 */
public enum ReconciliationStatus {
    /**
     * Calculated balance matches expected balance
     */
    MATCH,

    /**
     * Divergence detected between calculated and expected balance
     */
    MISMATCH
}
