package com.ledgerservice.api.dtos.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for reconciliation dashboard
 */
public record ReconciliationDashboardResponse(
        long totalAccounts,
        long accountsWithMismatch,
        BigDecimal largestDifference,
        List<ReconciliationSummary> recentReconciliations) {
    public record ReconciliationSummary(
            UUID accountId,
            BigDecimal expectedBalance,
            BigDecimal calculatedBalance,
            BigDecimal difference,
            String status,
            LocalDateTime createdAt) {
    }
}