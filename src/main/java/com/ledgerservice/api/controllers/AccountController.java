package com.ledgerservice.api.controllers;

import com.ledgerservice.api.dtos.response.BalanceResponse;
import com.ledgerservice.application.usecases.CalculateBalanceUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST Controller for account balance
 */
@RestController
@RequestMapping("/api/v1/accounts")
@Tag(name = "Accounts", description = "Account balance endpoints")
public class AccountController {

    private final CalculateBalanceUseCase calculateBalanceUseCase;

    public AccountController(CalculateBalanceUseCase calculateBalanceUseCase) {
        this.calculateBalanceUseCase = calculateBalanceUseCase;
    }

    @GetMapping("/{accountId}/balance")
    @Operation(summary = "Get account balance", description = "Calculates account balance in real-time by summing all entries. No cached value is used.")
    public ResponseEntity<BalanceResponse> getBalance(@PathVariable UUID accountId) {

        // Execute use case
        var result = calculateBalanceUseCase.execute(accountId);

        // Map to response DTO
        BalanceResponse response = new BalanceResponse(
                result.accountId(),
                result.balance().getValue(),
                result.entriesCount(),
                result.calculatedAt());

        return ResponseEntity.ok(response);
    }
}
