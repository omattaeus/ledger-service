package com.ledgerservice.domain.services;

import com.ledgerservice.domain.entities.Entry;
import com.ledgerservice.domain.enums.Direction;
import com.ledgerservice.domain.valueobjects.Money;

import java.util.Objects;
import java.util.UUID;

/**
 * Domain Service for creating ledger entries
 * 
 * Ensures correct double-entry bookkeeping:
 * - Debit entries have negative amounts
 * - Credit entries have positive amounts
 * - Entry types are consistent with operation semantics
 */
public class EntryFactory {

    /**
     * Creates a debit entry (money leaving account)
     * Amount will be negated to ensure negative value in ledger
     */
    public Entry createDebitEntry(
            UUID operationId,
            UUID accountId,
            Money amount,
            String entryType,
            String source) {
        Objects.requireNonNull(operationId, "Operation ID cannot be null");
        Objects.requireNonNull(accountId, "Account ID cannot be null");
        Objects.requireNonNull(amount, "Amount cannot be null");
        Objects.requireNonNull(entryType, "Entry type cannot be null");
        Objects.requireNonNull(source, "Source cannot be null");

        if (amount.isNegative() || amount.isZero()) {
            throw new IllegalArgumentException("Debit amount must be positive (will be negated internally)");
        }

        return Entry.create(
                operationId,
                accountId,
                amount.negate(), // Store as negative!
                Direction.DEBIT,
                entryType,
                source);
    }

    /**
     * Creates a credit entry (money entering account)
     * Amount will be stored as positive value
     */
    public Entry createCreditEntry(
            UUID operationId,
            UUID accountId,
            Money amount,
            String entryType,
            String source) {
        Objects.requireNonNull(operationId, "Operation ID cannot be null");
        Objects.requireNonNull(accountId, "Account ID cannot be null");
        Objects.requireNonNull(amount, "Amount cannot be null");
        Objects.requireNonNull(entryType, "Entry type cannot be null");
        Objects.requireNonNull(source, "Source cannot be null");

        if (amount.isNegative() || amount.isZero())
            throw new IllegalArgumentException("Credit amount must be positive");

        return Entry.create(
                operationId,
                accountId,
                amount, // Store as positive!
                Direction.CREDIT,
                entryType,
                source);
    }

    /**
     * Validates that double-entry bookkeeping rule is satisfied
     * Sum of all entry amounts must equal zero (conservation law)
     * 
     * @return true if entries balance out to zero
     */
    public boolean validateDoubleEntry(Entry... entries) {
        if (entries == null || entries.length == 0)
            return false;

        Money sum = Money.zero();
        for (Entry entry : entries) {
            sum = sum.add(entry.getAmount());
        }

        return sum.isZero();
    }
}