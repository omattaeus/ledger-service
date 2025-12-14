package com.ledgerservice.domain.entities;

import com.ledgerservice.domain.enums.OperationStatus;
import com.ledgerservice.domain.enums.OperationType;
import com.ledgerservice.domain.valueobjects.ExternalReference;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OperationTest {

    @Test
    void shouldCreateOperationInProcessingStatus() {
        ExternalReference ref = ExternalReference.of("PSP-123");
        Operation operation = Operation.create(ref, OperationType.DEPOSIT);

        assertNotNull(operation.getId());
        assertEquals(ref, operation.getExternalReference());
        assertEquals(OperationType.DEPOSIT, operation.getType());
        assertEquals(OperationStatus.PROCESSING, operation.getStatus());
        assertNotNull(operation.getCreatedAt());
        assertNull(operation.getProcessedAt());
        assertFalse(operation.isProcessed());
    }

    @Test
    void shouldMarkAsProcessed() {
        Operation operation = Operation.create(ExternalReference.of("PSP-123"), OperationType.DEPOSIT);

        operation.markAsProcessed();

        assertEquals(OperationStatus.PROCESSED, operation.getStatus());
        assertNotNull(operation.getProcessedAt());
        assertTrue(operation.isProcessed());
        assertNull(operation.getFailureReason());
    }

    @Test
    void shouldMarkAsIgnored() {
        Operation operation = Operation.create(ExternalReference.of("PSP-123"), OperationType.DEPOSIT);

        operation.markAsIgnored();

        assertEquals(OperationStatus.IGNORED, operation.getStatus());
        assertNotNull(operation.getProcessedAt());
        assertTrue(operation.isIgnored());
        assertNotNull(operation.getFailureReason());
    }

    @Test
    void shouldMarkAsFailed() {
        Operation operation = Operation.create(ExternalReference.of("PSP-123"), OperationType.DEPOSIT);

        operation.markAsFailed("Account not found");

        assertEquals(OperationStatus.FAILED, operation.getStatus());
        assertNotNull(operation.getProcessedAt());
        assertTrue(operation.isFailed());
        assertEquals("Account not found", operation.getFailureReason());
    }

    @Test
    void shouldThrowExceptionWhenMarkingProcessedTwice() {
        Operation operation = Operation.create(ExternalReference.of("PSP-123"), OperationType.DEPOSIT);
        operation.markAsProcessed();

        assertThrows(IllegalStateException.class, () -> operation.markAsProcessed());
    }

    @Test
    void shouldThrowExceptionWhenFailureReasonIsNull() {
        Operation operation = Operation.create(ExternalReference.of("PSP-123"), OperationType.DEPOSIT);

        assertThrows(NullPointerException.class, () -> operation.markAsFailed(null));
    }
}