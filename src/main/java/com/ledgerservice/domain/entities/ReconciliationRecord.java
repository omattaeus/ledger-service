package com.ledgerservice.domain.entities;

import com.ledgerservice.domain.enums.ReconciliationStatus;
import com.ledgerservice.domain.valueobjects.Money;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * ReconciliationRecord entity - tracks reconciliation attempts
 * Records divergences between internal calculated balance and external expected
 * balance
 * DOES NOT auto-correct - only detects and reports
 */
public class ReconciliationRecord {

    private final UUID id;
    private final UUID accountId;
    private final LocalDate reconciliationDate;
    private final Money expectedBalance;
    private final Money calculatedBalance;
    private final Money difference;
    private final ReconciliationStatus status;
    private final LocalDateTime createdAt;

    private ReconciliationRecord(
            UUID id,
            UUID accountId,
            LocalDate reconciliationDate,
            Money expectedBalance,
            Money calculatedBalance,
            Money difference,
            ReconciliationStatus status,
            LocalDateTime createdAt) {
        this.id = Objects.requireNonNull(id, "Reconciliation ID cannot be null");
        this.accountId = Objects.requireNonNull(accountId, "Account ID cannot be null");
        this.reconciliationDate = Objects.requireNonNull(reconciliationDate, "Reconciliation date cannot be null");
        this.expectedBalance = Objects.requireNonNull(expectedBalance, "Expected balance cannot be null");
        this.calculatedBalance = Objects.requireNonNull(calculatedBalance, "Calculated balance cannot be null");
        this.difference = Objects.requireNonNull(difference, "Difference cannot be null");
        this.status = Objects.requireNonNull(status, "Status cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "Created timestamp cannot be null");
    }

    /**
     * Creates a new reconciliation record comparing balances
     * 
     * @param accountId         Account being reconciled
     * @param expectedBalance   Balance according to external source (PSP/bank)
     * @param calculatedBalance Balance calculated from our entries
     * @return ReconciliationRecord with status MATCH or MISMATCH
     */
    public static ReconciliationRecord create(
            UUID accountId,
            Money expectedBalance,
            Money calculatedBalance) {
        Money difference = expectedBalance.subtract(calculatedBalance);
        ReconciliationStatus status = difference.isZero()
                ? ReconciliationStatus.MATCH
                : ReconciliationStatus.MISMATCH;

        return new ReconciliationRecord(
                UUID.randomUUID(),
                accountId,
                LocalDate.now(),
                expectedBalance,
                calculatedBalance,
                difference,
                status,
                LocalDateTime.now());
    }

    /**
     * Reconstitutes a reconciliation record from persistence
     */
    public static ReconciliationRecord reconstitute(
            UUID id,
            UUID accountId,
            LocalDate reconciliationDate,
            Money expectedBalance,
            Money calculatedBalance,
            Money difference,
            ReconciliationStatus status,
            LocalDateTime createdAt) {
        return new ReconciliationRecord(
                id, accountId, reconciliationDate, expectedBalance,
                calculatedBalance, difference, status, createdAt);
    }

    /**
     * Checks if balances match
     */
    public boolean isMatch() {
        return status == ReconciliationStatus.MATCH;
    }

    /**
     * Checks if there's a divergence
     */
    public boolean isMismatch() {
        return status == ReconciliationStatus.MISMATCH;
    }

    /**
     * Checks if we're missing money internally (positive difference)
     */
    public boolean isMissingMoney() {
        return difference.isPositive();
    }

    /**
     * Checks if we have extra money internally (negative difference)
     */
    public boolean hasExtraMoney() {
        return difference.isNegative();
    }

    public UUID getId() {
        return id;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public LocalDate getReconciliationDate() {
        return reconciliationDate;
    }

    public Money getExpectedBalance() {
        return expectedBalance;
    }

    public Money getCalculatedBalance() {
        return calculatedBalance;
    }

    public Money getDifference() {
        return difference;
    }

    public ReconciliationStatus getStatus() {
        return status;
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
        ReconciliationRecord that = (ReconciliationRecord) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("ReconciliationRecord[id=%s, account=%s, status=%s, difference=%s]",
                id, accountId, status, difference);
    }
}