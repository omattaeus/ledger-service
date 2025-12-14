package com.ledgerservice.api.controllers;

import com.ledgerservice.api.dtos.request.ReconciliationRequest;
import com.ledgerservice.api.dtos.response.ReconciliationResponse;
import com.ledgerservice.application.usecases.ReconcileAccountUseCase;
import com.ledgerservice.domain.valueobjects.Money;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for reconciliation
 */
@RestController
@RequestMapping("/api/v1/reconciliation")
@Tag(name = "Reconciliation", description = "Account reconciliation endpoints")
public class ReconciliationController {

    private final ReconcileAccountUseCase reconcileAccountUseCase;

    public ReconciliationController(ReconcileAccountUseCase reconcileAccountUseCase) {
        this.reconcileAccountUseCase = reconcileAccountUseCase;
    }

    @PostMapping
    @Operation(summary = "Reconcile account", description = "Compares expected balance with calculated balance. Detects divergences but does not auto-correct.")
    public ResponseEntity<ReconciliationResponse> reconcileAccount(@Valid @RequestBody ReconciliationRequest request) {

        // Execute use case
        var result = reconcileAccountUseCase.execute(
                request.accountId(),
                Money.of(request.expectedBalance()));

        // Map to response DTO
        ReconciliationResponse response = new ReconciliationResponse(
                result.accountId(),
                result.expectedBalance().getValue(),
                result.calculatedBalance().getValue(),
                result.difference().getValue(),
                result.status().name(),
                result.isMatch(),
                result.isMismatch());

        return ResponseEntity.ok(response);
    }
}
