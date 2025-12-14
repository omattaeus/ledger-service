package com.ledgerservice.api.controllers;

import com.ledgerservice.api.dtos.request.ReconciliationRequest;
import com.ledgerservice.api.dtos.response.DivergenceAnalysisResponse;
import com.ledgerservice.api.dtos.response.ReconciliationDashboardResponse;
import com.ledgerservice.api.dtos.response.ReconciliationResponse;
import com.ledgerservice.application.usecases.AnalyzeDivergenceUseCase;
import com.ledgerservice.application.usecases.ReconcileAccountUseCase;
import com.ledgerservice.domain.valueobjects.Money;
import com.ledgerservice.infrastructure.persistence.entities.ReconciliationRecordJpaEntity;
import com.ledgerservice.infrastructure.persistence.repositories.AccountJpaRepository;
import com.ledgerservice.infrastructure.persistence.repositories.ReconciliationRecordJpaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * REST Controller for reconciliation
 */
@RestController
@RequestMapping("/api/v1/reconciliation")
@Tag(name = "Reconciliation", description = "Account reconciliation endpoints")
public class ReconciliationController {

    private final ReconcileAccountUseCase reconcileAccountUseCase;
    private final AnalyzeDivergenceUseCase analyzeDivergenceUseCase;
    private final ReconciliationRecordJpaRepository reconciliationRepository;
    private final AccountJpaRepository accountRepository;

    public ReconciliationController(
            ReconcileAccountUseCase reconcileAccountUseCase,
            AnalyzeDivergenceUseCase analyzeDivergenceUseCase,
            ReconciliationRecordJpaRepository reconciliationRepository,
            AccountJpaRepository accountRepository) {
        this.reconcileAccountUseCase = reconcileAccountUseCase;
        this.analyzeDivergenceUseCase = analyzeDivergenceUseCase;
        this.reconciliationRepository = reconciliationRepository;
        this.accountRepository = accountRepository;
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

    @GetMapping("/{accountId}")
    @Operation(summary = "Get reconciliation history", description = "Returns all reconciliation records for an account, ordered by date descending")
    public ResponseEntity<List<ReconciliationResponse>> getReconciliationHistory(@PathVariable UUID accountId) {

        List<ReconciliationRecordJpaEntity> records = reconciliationRepository
                .findByAccountIdOrderByCreatedAtDesc(accountId);

        List<ReconciliationResponse> responses = records.stream()
                .map(rec -> new ReconciliationResponse(
                        rec.getAccountId(),
                        rec.getExpectedBalance(),
                        rec.getCalculatedBalance(),
                        rec.getDifference(),
                        rec.getStatus().name(),
                        rec.getStatus().name().equals("MATCH"),
                        rec.getStatus().name().equals("MISMATCH")))
                .toList();

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Reconciliation dashboard", description = "Returns statistics and recent reconciliations across all accounts")
    public ResponseEntity<ReconciliationDashboardResponse> getDashboard() {

        // Total accounts
        long totalAccounts = accountRepository.count();

        // Accounts with mismatches (distinct account_ids with MISMATCH status)
        long accountsWithMismatch = reconciliationRepository
                .findAll()
                .stream()
                .filter(rec -> "MISMATCH".equals(rec.getStatus().name()))
                .map(ReconciliationRecordJpaEntity::getAccountId)
                .distinct()
                .count();

        // Largest difference
        BigDecimal largestDifference = reconciliationRepository
                .findAll()
                .stream()
                .map(rec -> rec.getDifference().abs())
                .max(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);

        // Recent reconciliations (last 10)
        List<ReconciliationRecordJpaEntity> recentRecords = reconciliationRepository
                .findAll(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")))
                .getContent();

        List<ReconciliationDashboardResponse.ReconciliationSummary> recentSummaries = recentRecords.stream()
                .map(rec -> new ReconciliationDashboardResponse.ReconciliationSummary(
                        rec.getAccountId(),
                        rec.getExpectedBalance(),
                        rec.getCalculatedBalance(),
                        rec.getDifference(),
                        rec.getStatus().name(),
                        rec.getCreatedAt()))
                .toList();

        ReconciliationDashboardResponse response = new ReconciliationDashboardResponse(
                totalAccounts,
                accountsWithMismatch,
                largestDifference,
                recentSummaries);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/divergence/{reconciliationId}")
    @Operation(summary = "Analyze divergence", description = "Analyzes a specific reconciliation divergence, showing recent entries timeline")
    public ResponseEntity<DivergenceAnalysisResponse> analyzeDivergence(
            @PathVariable UUID reconciliationId,
            @RequestParam(defaultValue = "20") int entryLimit) {

        var result = analyzeDivergenceUseCase.execute(reconciliationId, entryLimit);

        var entryDetails = result.recentEntries().stream()
                .map(entry -> new DivergenceAnalysisResponse.EntryDetail(
                        entry.getId(),
                        entry.getOperationId(),
                        entry.getAmount(),
                        entry.getDirection().name(),
                        entry.getEntryType(),
                        entry.getSource(),
                        entry.getCreatedAt()))
                .toList();

        DivergenceAnalysisResponse response = new DivergenceAnalysisResponse(
                result.reconciliation().getAccountId(),
                result.reconciliation().getExpectedBalance().getValue(),
                result.reconciliation().getCalculatedBalance().getValue(),
                result.reconciliation().getDifference().getValue(),
                result.reconciliation().getReconciliationDate().atStartOfDay(), // Convert LocalDate to LocalDateTime
                entryDetails,
                result.analysis());

        return ResponseEntity.ok(response);
    }
}
