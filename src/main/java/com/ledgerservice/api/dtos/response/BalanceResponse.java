package com.ledgerservice.api.dtos.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for balance calculation
 */
public record BalanceResponse(
        UUID accountId,
        BigDecimal balance,
        long entriesCount,
        LocalDateTime calculatedAt) {
}