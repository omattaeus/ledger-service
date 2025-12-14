package com.ledgerservice.api.dtos.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request DTO for reconciliation
 */
public record ReconciliationRequest(

        @NotNull(message = "Account ID is required") UUID accountId,
        @NotNull(message = "Expected balance is required") @Positive(message = "Expected balance must be positive") BigDecimal expectedBalance) {
}