package com.ledgerservice.domain.services;

import com.ledgerservice.domain.entities.Entry;
import com.ledgerservice.domain.valueobjects.Money;

import java.util.List;
import java.util.Objects;

/**
 * Domain Service for calculating account balances
 * 
 * PRINCIPLE: Balance is ALWAYS calculated from entries, never cached or stored
 * This ensures:
 * - Auditability (balance is derived from immutable facts)
 * - Consistency (impossible to have divergent balance)
 * - Time-travel (can calculate balance at any point in history)
 */
public class BalanceCalculator {

    /**
     * Calculates current balance from all entries
     * 
     * @param entries All entries for the account (must not be null)
     * @return Calculated balance as Money
     */
    public Money calculateBalance(List<Entry> entries) {
        Objects.requireNonNull(entries, "Entries list cannot be null");

        return entries.stream()
                .map(Entry::getAmount)
                .reduce(Money.zero(), Money::add);
    }

    /**
     * Calculates balance at a specific point in time
     * Only considers entries created before or at the given timestamp
     * 
     * @param entries All entries for the account
     * @param upTo    Cutoff time (inclusive)
     * @return Historical balance as Money
     */
    public Money calculateBalanceUpTo(List<Entry> entries, java.time.LocalDateTime upTo) {
        Objects.requireNonNull(entries, "Entries list cannot be null");
        Objects.requireNonNull(upTo, "Cutoff time cannot be null");

        return entries.stream()
                .filter(entry -> !entry.getCreatedAt().isAfter(upTo))
                .map(Entry::getAmount)
                .reduce(Money.zero(), Money::add);
    }

    /**
     * Counts total number of entries
     * Useful for audit and reporting
     */
    public long countEntries(List<Entry> entries) {
        Objects.requireNonNull(entries, "Entries list cannot be null");
        return entries.size();
    }
}