package com.ledgerservice.api.dtos.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for divergence analysis
 */
public record DivergenceAnalysisResponse(
        UUID accountId,
        BigDecimal expectedBalance,
        BigDecimal calculatedBalance,
        BigDecimal difference,
        LocalDateTime reconciliationDate,
        List<EntryDetail> recentEntries,
        String analysis) {
    public record EntryDetail(
            UUID entryId,
            UUID operationId,
            BigDecimal amount,
            String direction,
            String entryType,
            String source,
            LocalDateTime createdAt) {
    }
}