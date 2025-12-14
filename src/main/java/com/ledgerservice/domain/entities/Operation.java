package com.ledgerservice.domain.entities;

import com.ledgerservice.domain.enums.OperationStatus;
import com.ledgerservice.domain.enums.OperationType;
import com.ledgerservice.domain.valueobjects.ExternalReference;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Operation entity - represents a financial operation with idempotency
 * guarantee
 * ExternalReference ensures idempotency - same operation can be submitted N
 * times safely
 */
public class Operation {

    private final UUID id;
    private final ExternalReference externalReference;
    private final OperationType type;
    private OperationStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime processedAt;
    private String failureReason;

    private Operation(
            UUID id,
            ExternalReference externalReference,
            OperationType type,
            OperationStatus status,
            LocalDateTime createdAt,
            LocalDateTime processedAt,
            String failureReason) {
        this.id = Objects.requireNonNull(id, "Operation ID cannot be null");
        this.externalReference = Objects.requireNonNull(externalReference, "External reference cannot be null");
        this.type = Objects.requireNonNull(type, "Operation type cannot be null");
        this.status = Objects.requireNonNull(status, "Operation status cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "Created timestamp cannot be null");
        this.processedAt = processedAt;
        this.failureReason = failureReason;
    }

    /**
     * Creates a new operation in PROCESSING status
     */
    public static Operation create(ExternalReference externalReference, OperationType type) {
        return new Operation(
                UUID.randomUUID(),
                externalReference,
                type,
                OperationStatus.PROCESSING,
                LocalDateTime.now(),
                null,
                null);
    }

    /**
     * Reconstitutes an operation from persistence
     */
    public static Operation reconstitute(
            UUID id,
            ExternalReference externalReference,
            OperationType type,
            OperationStatus status,
            LocalDateTime createdAt,
            LocalDateTime processedAt,
            String failureReason) {
        return new Operation(id, externalReference, type, status, createdAt, processedAt, failureReason);
    }

    /**
     * Marks operation as successfully processed
     */
    public void markAsProcessed() {
        if (this.status == OperationStatus.PROCESSED) {
            throw new IllegalStateException("Operation is already processed");
        }
        this.status = OperationStatus.PROCESSED;
        this.processedAt = LocalDateTime.now();
        this.failureReason = null;
    }

    /**
     * Marks operation as ignored (duplicate detected)
     */
    public void markAsIgnored() {
        this.status = OperationStatus.IGNORED;
        this.processedAt = LocalDateTime.now();
        this.failureReason = "Duplicate operation detected";
    }

    /**
     * Marks operation as failed with reason
     */
    public void markAsFailed(String reason) {
        Objects.requireNonNull(reason, "Failure reason cannot be null");
        this.status = OperationStatus.FAILED;
        this.processedAt = LocalDateTime.now();
        this.failureReason = reason;
    }

    /**
     * Checks if operation is already processed (for idempotency)
     */
    public boolean isProcessed() {
        return status == OperationStatus.PROCESSED;
    }

    /**
     * Checks if operation is ignored (duplicate)
     */
    public boolean isIgnored() {
        return status == OperationStatus.IGNORED;
    }

    /**
     * Checks if operation failed
     */
    public boolean isFailed() {
        return status == OperationStatus.FAILED;
    }

    public UUID getId() {
        return id;
    }

    public ExternalReference getExternalReference() {
        return externalReference;
    }

    public OperationType getType() {
        return type;
    }

    public OperationStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public String getFailureReason() {
        return failureReason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Operation operation = (Operation) o;
        return id.equals(operation.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Operation[id=%s, externalRef=%s, type=%s, status=%s]",
                id, externalReference, type, status);
    }
}