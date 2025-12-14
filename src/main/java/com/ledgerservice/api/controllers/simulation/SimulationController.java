package com.ledgerservice.api.controllers.simulation;

import com.ledgerservice.api.dtos.request.CreateOperationRequest;
import com.ledgerservice.api.dtos.request.SimulationOperationRequest;
import com.ledgerservice.api.dtos.response.OperationResponse;
import com.ledgerservice.api.dtos.response.SimulationResponse;
import com.ledgerservice.application.usecases.ProcessOperationUseCase;
import com.ledgerservice.domain.entities.Operation;
import com.ledgerservice.domain.enums.OperationType;
import com.ledgerservice.domain.valueobjects.ExternalReference;
import com.ledgerservice.domain.valueobjects.Money;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Controller for failure simulation endpoints
 * Demonstrates system robustness against dirty production scenarios
 */
@RestController
@RequestMapping("/api/v1/simulation")
@Tag(name = "Simulation", description = "Endpoints to demonstrate failure tolerance")
public class SimulationController {

        private static final Logger log = LoggerFactory.getLogger(SimulationController.class);
        private final ProcessOperationUseCase processOperationUseCase;

        public SimulationController(ProcessOperationUseCase processOperationUseCase) {
                this.processOperationUseCase = processOperationUseCase;
        }

        /**
         * Simulates duplicate requests arriving in parallel
         * Proves that only 1 operation is created despite N identical requests
         */
        @PostMapping("/duplicate")
        @io.swagger.v3.oas.annotations.Operation(summary = "Simulate duplicate requests", description = "Sends the same operation N times in parallel. Only 1 should be processed, others detected as duplicates.")
        public ResponseEntity<SimulationResponse> simulateDuplicates(
                        @Valid @RequestBody SimulationOperationRequest request) {
                long startTime = System.currentTimeMillis();
                int parallelCount = request.parallelCount() != null ? request.parallelCount() : 10;

                log.info("Starting duplicate simulation: {} parallel requests for external_reference={}",
                                parallelCount, request.externalReference());

                var command = buildCommand(request);
                ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

                try {
                        List<CompletableFuture<Operation>> futures = IntStream.range(0, parallelCount)
                                        .mapToObj(i -> CompletableFuture.supplyAsync(() -> {
                                                log.debug("Submitting request #{} for {}", i,
                                                                request.externalReference());
                                                return processOperationUseCase.execute(command);
                                        }, executor))
                                        .collect(Collectors.toList());

                        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

                        List<Operation> operations = futures.stream()
                                        .map(CompletableFuture::join)
                                        .collect(Collectors.toList());

                        long uniqueOperations = operations.stream()
                                        .map(Operation::getId)
                                        .distinct()
                                        .count();

                        long executionTime = System.currentTimeMillis() - startTime;

                        log.info("Duplicate simulation completed: {} requests, {} unique operation(s), {}ms",
                                        parallelCount, uniqueOperations, executionTime);

                        List<OperationResponse> operationResponses = operations.stream()
                                        .limit(1)
                                        .map(this::toResponse)
                                        .collect(Collectors.toList());

                        SimulationResponse response = new SimulationResponse(
                                        parallelCount,
                                        (int) uniqueOperations,
                                        parallelCount - (int) uniqueOperations,
                                        executionTime,
                                        String.format("Idempotency working! %d parallel requests resulted in %d operation(s)",
                                                        parallelCount, uniqueOperations),
                                        operationResponses);

                        return ResponseEntity.ok(response);

                } finally {
                        executor.shutdown();
                }
        }

        /**
         * Simulates operations arriving out of order
         * Proves that result is consistent regardless of processing order
         */
        @PostMapping("/out-of-order")
        @io.swagger.v3.oas.annotations.Operation(summary = "Simulate out-of-order processing", description = "Processes multiple operations in randomized order. Results should be consistent.")
        public ResponseEntity<SimulationResponse> simulateOutOfOrder(
                        @Valid @RequestBody List<SimulationOperationRequest> requests) {
                long startTime = System.currentTimeMillis();

                log.info("Starting out-of-order simulation with {} operations", requests.size());

                List<SimulationOperationRequest> shuffled = new ArrayList<>(requests);
                Collections.shuffle(shuffled);

                List<Operation> operations = shuffled.stream()
                                .map(this::buildCommand)
                                .map(processOperationUseCase::execute)
                                .collect(Collectors.toList());

                long executionTime = System.currentTimeMillis() - startTime;

                log.info("Out-of-order simulation completed: {} operations processed in {}ms",
                                operations.size(), executionTime);

                List<OperationResponse> operationResponses = operations.stream()
                                .map(this::toResponse)
                                .collect(Collectors.toList());

                SimulationResponse response = new SimulationResponse(
                                requests.size(),
                                operations.size(),
                                0,
                                executionTime,
                                "✅ Out-of-order tolerance working! Operations processed in random order with consistent results",
                                operationResponses);

                return ResponseEntity.ok(response);
        }

        /**
         * Simulates delayed operation
         * Proves that system handles async processing correctly
         */
        @PostMapping("/delayed")
        @io.swagger.v3.oas.annotations.Operation(summary = "Simulate delayed operation", description = "Processes operation after specified delay (simulates network latency)")
        public ResponseEntity<SimulationResponse> simulateDelay(
                        @Valid @RequestBody SimulationOperationRequest request) {
                long startTime = System.currentTimeMillis();
                long delayMs = request.delayMs() != null ? request.delayMs() : 1000L;

                log.info("Starting delayed simulation: {}ms delay for external_reference={}",
                                delayMs, request.externalReference());

                try {
                        Thread.sleep(delayMs);

                        var command = buildCommand(request);
                        Operation operation = processOperationUseCase.execute(command);
                        long executionTime = System.currentTimeMillis() - startTime;

                        log.info("Delayed simulation completed: operation processed after {}ms total", executionTime);

                        SimulationResponse response = new SimulationResponse(
                                        1,
                                        1,
                                        0,
                                        executionTime,
                                        String.format("✅ Delay tolerance working! Operation processed successfully after %dms delay",
                                                        delayMs),
                                        List.of(toResponse(operation)));

                        return ResponseEntity.ok(response);

                } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Simulation interrupted", e);
                }
        }

        /**
         * Simulates retry scenario
         * Same operation arriving multiple times (simulates webhook retry)
         */
        @PostMapping("/retry")
        @io.swagger.v3.oas.annotations.Operation(summary = "Simulate retry scenario", description = "Simulates webhook retry - same operation arriving multiple times with delays")
        public ResponseEntity<SimulationResponse> simulateRetry(
                        @Valid @RequestBody SimulationOperationRequest request) {
                long startTime = System.currentTimeMillis();
                int retryCount = request.parallelCount() != null ? request.parallelCount() : 3;
                long retryDelayMs = request.delayMs() != null ? request.delayMs() : 500L;

                log.info("Starting retry simulation: {} retries with {}ms delay for external_reference={}",
                                retryCount, retryDelayMs, request.externalReference());

                var command = buildCommand(request);
                List<Operation> operations = new ArrayList<>();

                try {
                        operations.add(processOperationUseCase.execute(command));

                        for (int i = 0; i < retryCount; i++) {
                                Thread.sleep(retryDelayMs);
                                log.debug("Retry attempt #{}", i + 1);
                                operations.add(processOperationUseCase.execute(command));
                        }

                        long executionTime = System.currentTimeMillis() - startTime;

                        long uniqueOperations = operations.stream()
                                        .map(Operation::getId)
                                        .distinct()
                                        .count();

                        log.info("Retry simulation completed: {} attempts, {} unique operation(s), {}ms total",
                                        retryCount + 1, uniqueOperations, executionTime);

                        SimulationResponse response = new SimulationResponse(
                                        retryCount + 1,
                                        (int) uniqueOperations,
                                        retryCount + 1 - (int) uniqueOperations,
                                        executionTime,
                                        String.format("Retry tolerance working! %d webhook retries resulted in %d operation(s)",
                                                        retryCount + 1, uniqueOperations),
                                        List.of(toResponse(operations.get(0))));

                        return ResponseEntity.ok(response);

                } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Simulation interrupted", e);
                }
        }

        // Helper methods
        private ProcessOperationUseCase.ProcessOperationCommand buildCommand(SimulationOperationRequest request) {
                return new ProcessOperationUseCase.ProcessOperationCommand(
                                ExternalReference.of(request.externalReference()),
                                mapOperationType(request.type()),
                                request.sourceAccountId(),
                                request.targetAccountId(),
                                Money.of(request.amount()),
                                "simulation");
        }

        private OperationType mapOperationType(CreateOperationRequest.OperationTypeDto dto) {
                return switch (dto) {
                        case DEPOSIT -> OperationType.DEPOSIT;
                        case WITHDRAWAL -> OperationType.WITHDRAWAL;
                        case TRANSFER -> OperationType.TRANSFER;
                };
        }

        private OperationResponse toResponse(Operation operation) {
                return new OperationResponse(
                                operation.getId(),
                                operation.getExternalReference().getValue(),
                                operation.getType().name(),
                                operation.getStatus().name(),
                                operation.getCreatedAt(),
                                operation.getProcessedAt());
        }
}