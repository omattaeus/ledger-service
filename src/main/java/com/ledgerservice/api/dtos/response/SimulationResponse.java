package com.ledgerservice.api.dtos.response;

import java.util.List;

/**
 * Response DTO for simulation results
 */
public record SimulationResponse(
        int totalRequests,
        int successfulOperations,
        int duplicateDetected,
        long executionTimeMs,
        String message,
        List<OperationResponse> operations) {
}
