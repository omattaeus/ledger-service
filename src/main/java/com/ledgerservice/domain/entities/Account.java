package com.ledgerservice.domain.entities;

import com.ledgerservice.domain.enums.AccountType;
import com.ledgerservice.domain.valueobjects.Money;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Account entity - represents a ledger account WITHOUT balance column
 * Balance is ALWAYS calculated from entries, never stored
 */
public class Account {

    private final UUID id;
    private final AccountType type;
    private final LocalDateTime createdAt;

    private Account(UUID id, AccountType type, LocalDateTime createdAt) {
        this.id = Objects.requireNonNull(id, "Account ID cannot be null");
        this.type = Objects.requireNonNull(type, "Account type cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "Created timestamp cannot be null");
    }

    /**
     * Creates a new account (for new records)
     */
    public static Account create(AccountType type) {
        return new Account(
                UUID.randomUUID(),
                type,
                LocalDateTime.now());
    }

    /**
     * Reconstitutes an account from persistence (for existing records)
     */
    public static Account reconstitute(UUID id, AccountType type, LocalDateTime createdAt) {
        return new Account(id, type, createdAt);
    }

    /**
     * Calculates balance from list of entries
     * This is the ONLY way to get account balance - never from a stored column!
     * 
     * @param entries All entries for this account
     * @return Calculated balance as Money
     */
    public Money calculateBalance(List<Entry> entries) {
        Objects.requireNonNull(entries, "Entries list cannot be null");

        return entries.stream()
                .map(Entry::getAmount)
                .reduce(Money.zero(), Money::add);
    }

    public UUID getId() {
        return id;
    }

    public AccountType getType() {
        return type;
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
        Account account = (Account) o;
        return id.equals(account.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Account[id=%s, type=%s, createdAt=%s]", id, type, createdAt);
    }
}