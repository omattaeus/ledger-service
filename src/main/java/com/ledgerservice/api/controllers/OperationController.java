package com.ledgerservice.api.controllers;

import com.ledgerservice.api.dtos.request.CreateOperationRequest;
import com.ledgerservice.api.dtos.response.OperationResponse;
import com.ledgerservice.application.usecases.ProcessOperationUseCase;
import com.ledgerservice.domain.enums.OperationType;
import com.ledgerservice.domain.valueobjects.ExternalReference;
import com.ledgerservice.domain.valueobjects.Money;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for operations
 */
@RestController
@RequestMapping("/api/v1/operations")
@Tag(name = "Operations", description = "Financial operations endpoints")
public class OperationController {

    private final ProcessOperationUseCase processOperationUseCase;

    public OperationController(ProcessOperationUseCase processOperationUseCase) {
        this.processOperationUseCase = processOperationUseCase;
    }

    @PostMapping
    @Operation(summary = "Create a new operation", description = "Creates a new financial operation (deposit, withdrawal, or transfer). Idempotent based on external reference.")
    public ResponseEntity<OperationResponse> createOperation(@Valid @RequestBody CreateOperationRequest request) {

        // Map DTO to use case command
        var command = new ProcessOperationUseCase.ProcessOperationCommand(
                ExternalReference.of(request.externalReference()),
                mapOperationType(request.type()),
                request.sourceAccountId(),
                request.targetAccountId(),
                Money.of(request.amount()),
                request.source());

        // Execute use case
        com.ledgerservice.domain.entities.Operation operation = processOperationUseCase.execute(command);

        // Map to response DTO
        OperationResponse response = new OperationResponse(
                operation.getId(),
                operation.getExternalReference().getValue(),
                operation.getType().name(),
                operation.getStatus().name(),
                operation.getCreatedAt(),
                operation.getProcessedAt());

        // Return 200 OK for idempotent requests (already processed)
        // In production, you might want to check if operation was just created
        return ResponseEntity.ok(response);
    }

    private OperationType mapOperationType(CreateOperationRequest.OperationTypeDto dto) {
        return switch (dto) {
            case DEPOSIT -> OperationType.DEPOSIT;
            case WITHDRAWAL -> OperationType.WITHDRAWAL;
            case TRANSFER -> OperationType.TRANSFER;
        };
    }
}