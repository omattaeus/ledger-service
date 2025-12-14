package com.ledgerservice.api.dtos.response;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for operation
 */
public record OperationResponse(
        UUID id,
        String externalReference,
        String type,
        String status,
        LocalDateTime createdAt,
        LocalDateTime processedAt) {
}