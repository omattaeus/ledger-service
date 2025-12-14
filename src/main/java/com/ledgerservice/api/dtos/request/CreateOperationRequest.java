package com.ledgerservice.api.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request DTO for creating an operation
 */
public record CreateOperationRequest(

        @NotBlank(message = "External reference is required") @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "External reference can only contain alphanumeric characters, hyphens, and underscores") String externalReference,
        @NotNull(message = "Operation type is required") OperationTypeDto type,

        UUID sourceAccountId, // Optional for deposits
        UUID targetAccountId, // Optional for withdrawals

        @NotNull(message = "Amount is required") @Positive(message = "Amount must be positive") BigDecimal amount,

        String source) {
    public enum OperationTypeDto {
        DEPOSIT,
        WITHDRAWAL,
        TRANSFER
    }
}