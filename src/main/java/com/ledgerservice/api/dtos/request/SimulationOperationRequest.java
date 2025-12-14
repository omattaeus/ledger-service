package com.ledgerservice.api.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request DTO for simulation endpoints
 */
public record SimulationOperationRequest(

        @NotBlank(message = "External reference is required") String externalReference,
        @NotNull(message = "Operation type is required") CreateOperationRequest.OperationTypeDto type,

        UUID sourceAccountId,
        UUID targetAccountId,

        @NotNull(message = "Amount is required") @Positive(message = "Amount must be positive") BigDecimal amount,
        @Positive(message = "Parallel count must be positive") Integer parallelCount, // For duplicate simulation
        @Positive(message = "Delay must be positive") Long delayMs // For delayed simulation
) {
}