package com.ledgerservice.api.dtos.response;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Response DTO for reconciliation
 */
public record ReconciliationResponse(
        UUID accountId,
        BigDecimal expectedBalance,
        BigDecimal calculatedBalance,
        BigDecimal difference,
        String status,
        boolean isMatch,
        boolean isMismatch) {
}
