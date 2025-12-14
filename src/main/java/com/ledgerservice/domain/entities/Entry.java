package com.ledgerservice.domain.entities;

import com.ledgerservice.domain.enums.Direction;
import com.ledgerservice.domain.valueobjects.Money;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entry entity - represents a ledger entry (double-entry bookkeeping)
 * Entries are IMMUTABLE - once created, never modified or deleted
 */
public class Entry {

    private final UUID id;
    private final UUID operationId;
    private final UUID accountId;
    private final Money amount;
    private final Direction direction;
    private final String entryType;
    private final String source;
    private final LocalDateTime createdAt;

    private Entry(
            UUID id,
            UUID operationId,
            UUID accountId,
            Money amount,
            Direction direction,
            String entryType,
            String source,
            LocalDateTime createdAt) {
        this.id = Objects.requireNonNull(id, "Entry ID cannot be null");
        this.operationId = Objects.requireNonNull(operationId, "Operation ID cannot be null");
        this.accountId = Objects.requireNonNull(accountId, "Account ID cannot be null");
        this.amount = Objects.requireNonNull(amount, "Amount cannot be null");
        this.direction = Objects.requireNonNull(direction, "Direction cannot be null");
        this.entryType = Objects.requireNonNull(entryType, "Entry type cannot be null");
        this.source = Objects.requireNonNull(source, "Source cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "Created timestamp cannot be null");

        // Business rule: amount cannot be zero
        if (amount.isZero())
            throw new IllegalArgumentException("Entry amount cannot be zero");
    }

    /**
     * Creates a new entry (for new records)
     */
    public static Entry create(
            UUID operationId,
            UUID accountId,
            Money amount,
            Direction direction,
            String entryType,
            String source) {
        return new Entry(
                UUID.randomUUID(),
                operationId,
                accountId,
                amount,
                direction,
                entryType,
                source,
                LocalDateTime.now());
    }

    /**
     * Reconstitutes an entry from persistence (for existing records)
     */
    public static Entry reconstitute(
            UUID id,
            UUID operationId,
            UUID accountId,
            Money amount,
            Direction direction,
            String entryType,
            String source,
            LocalDateTime createdAt) {
        return new Entry(id, operationId, accountId, amount, direction, entryType, source, createdAt);
    }

    /**
     * Checks if this is a credit entry (money in)
     */
    public boolean isCredit() {
        return direction == Direction.CREDIT;
    }

    /**
     * Checks if this is a debit entry (money out)
     */
    public boolean isDebit() {
        return direction == Direction.DEBIT;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOperationId() {
        return operationId;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public Money getAmount() {
        return amount;
    }

    public Direction getDirection() {
        return direction;
    }

    public String getEntryType() {
        return entryType;
    }

    public String getSource() {
        return source;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Entry entry = (Entry) o;
        return id.equals(entry.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Entry[id=%s, operation=%s, account=%s, amount=%s, direction=%s, type=%s]",
                id, operationId, accountId, amount, direction, entryType);
    }
}