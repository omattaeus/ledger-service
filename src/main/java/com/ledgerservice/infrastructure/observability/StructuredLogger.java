package com.ledgerservice.infrastructure.observability;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Structured event logger for financial operations.
 * Logs important business events in a structured format for monitoring and
 * auditing.
 */
@Component
public class StructuredLogger {

    private static final Logger log = LoggerFactory.getLogger(StructuredLogger.class);

    /**
     * Logs when an operation is received
     */
    public void logOperationReceived(String externalReference, String operationType, BigDecimal amount) {
        Map<String, Object> event = new HashMap<>();
        event.put("event", "operation.received");
        event.put("externalReference", externalReference);
        event.put("operationType", operationType);
        event.put("amount", amount);

        log.info("Operation received: {}", formatEvent(event));
    }

    /**
     * Logs when a duplicate operation is detected
     */
    public void logDuplicateDetected(String externalReference, UUID existingOperationId) {
        Map<String, Object> event = new HashMap<>();
        event.put("event", "operation.duplicate_detected");
        event.put("externalReference", externalReference);
        event.put("existingOperationId", existingOperationId);

        log.info("Duplicate operation detected: {}", formatEvent(event));
    }

    /**
     * Logs when an operation is successfully processed
     */
    public void logOperationProcessed(UUID operationId, String externalReference, String operationType,
            BigDecimal amount) {
        Map<String, Object> event = new HashMap<>();
        event.put("event", "operation.processed");
        event.put("operationId", operationId);
        event.put("externalReference", externalReference);
        event.put("operationType", operationType);
        event.put("amount", amount);

        log.info("Operation processed successfully: {}", formatEvent(event));
    }

    /**
     * Logs when an operation fails
     */
    public void logOperationFailed(String externalReference, String reason, Exception exception) {
        Map<String, Object> event = new HashMap<>();
        event.put("event", "operation.failed");
        event.put("externalReference", externalReference);
        event.put("reason", reason);
        event.put("exceptionClass", exception.getClass().getSimpleName());
        event.put("exceptionMessage", exception.getMessage());

        log.error("Operation failed: {}", formatEvent(event), exception);
    }

    /**
     * Logs when a reconciliation mismatch is detected
     */
    public void logReconciliationMismatch(UUID accountId, BigDecimal expected, BigDecimal calculated,
            BigDecimal difference) {
        Map<String, Object> event = new HashMap<>();
        event.put("event", "reconciliation.mismatch");
        event.put("accountId", accountId);
        event.put("expectedBalance", expected);
        event.put("calculatedBalance", calculated);
        event.put("difference", difference);

        log.warn("Reconciliation mismatch detected: {}", formatEvent(event));
    }

    /**
     * Logs when a reconciliation match is confirmed
     */
    public void logReconciliationMatch(UUID accountId, BigDecimal balance) {
        Map<String, Object> event = new HashMap<>();
        event.put("event", "reconciliation.match");
        event.put("accountId", accountId);
        event.put("balance", balance);

        log.info("Reconciliation match confirmed: {}", formatEvent(event));
    }

    /**
     * Logs when balance is calculated
     */
    public void logBalanceCalculated(UUID accountId, BigDecimal balance, int entriesCount) {
        Map<String, Object> event = new HashMap<>();
        event.put("event", "balance.calculated");
        event.put("accountId", accountId);
        event.put("balance", balance);
        event.put("entriesCount", entriesCount);

        log.debug("Balance calculated: {}", formatEvent(event));
    }

    private String formatEvent(Map<String, Object> event) {
        // Simple key=value format for readability
        // In production, this could be JSON for better parsing
        StringBuilder sb = new StringBuilder();
        event.forEach((key, value) -> {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(key).append("=").append(value);
        });
        return sb.toString();
    }
}