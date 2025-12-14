package com.ledgerservice.simulation;

import com.ledgerservice.application.usecases.CalculateBalanceUseCase;
import com.ledgerservice.application.usecases.ProcessOperationUseCase;
import com.ledgerservice.domain.entities.Account;
import com.ledgerservice.domain.enums.AccountType;
import com.ledgerservice.domain.enums.OperationType;
import com.ledgerservice.domain.valueobjects.ExternalReference;
import com.ledgerservice.domain.valueobjects.Money;
import com.ledgerservice.infrastructure.persistence.mappers.EntityMapper;
import com.ledgerservice.infrastructure.persistence.repositories.AccountJpaRepository;
import com.ledgerservice.infrastructure.persistence.repositories.EntryJpaRepository;
import com.ledgerservice.infrastructure.persistence.repositories.OperationJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test demonstrating duplicate operation handling
 * Sends 100 identical requests in parallel - only 1 should be processed
 */
@SpringBootTest
@ActiveProfiles("test")
class DuplicateOperationIntegrationTest {

    @Autowired
    private ProcessOperationUseCase processOperationUseCase;

    @Autowired
    private CalculateBalanceUseCase calculateBalanceUseCase;

    @Autowired
    private AccountJpaRepository accountRepository;

    @Autowired
    private OperationJpaRepository operationRepository;

    @Autowired
    private EntryJpaRepository entryRepository;

    private UUID accountId;

    @BeforeEach
    void setUp() {
        // Clean database
        entryRepository.deleteAll();
        operationRepository.deleteAll();
        accountRepository.deleteAll();

        // Create test account
        Account account = Account.create(AccountType.USER);
        accountRepository.save(EntityMapper.toJpa(account));
        accountId = account.getId();
    }

    @Test
    void shouldHandleDuplicateRequestsCorrectly() {
        // Given - 100 identical requests in parallel
        String externalRef = "DUP-TEST-" + UUID.randomUUID();
        int parallelRequests = 100;

        var command = new ProcessOperationUseCase.ProcessOperationCommand(
                ExternalReference.of(externalRef),
                OperationType.DEPOSIT,
                null,
                accountId,
                Money.of("100.00"),
                "duplicate_test");

        // When - execute in parallel using virtual threads
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

        List<CompletableFuture<com.ledgerservice.domain.entities.Operation>> futures = IntStream
                .range(0, parallelRequests)
                .mapToObj(i -> CompletableFuture.supplyAsync(() -> processOperationUseCase.execute(command), executor))
                .collect(Collectors.toList());

        // Wait for all
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        List<com.ledgerservice.domain.entities.Operation> operations = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        executor.shutdown();

        // Then - verify only 1 operation was created
        long uniqueOperationIds = operations.stream()
                .map(com.ledgerservice.domain.entities.Operation::getId)
                .distinct()
                .count();

        assertEquals(1, uniqueOperationIds,
                "Should have exactly 1 unique operation despite " + parallelRequests + " requests");

        // Verify database state
        long operationCount = operationRepository.count();
        assertEquals(1, operationCount, "Database should contain exactly 1 operation");

        // Verify balance is correct (only ONE deposit of 100)
        var balanceResult = calculateBalanceUseCase.execute(accountId);
        assertEquals(Money.of("100.00"), balanceResult.balance(),
                "Balance should be 100.00, not " + (parallelRequests * 100));

        // Verify entries count (1 credit entry)
        assertEquals(1, balanceResult.entriesCount(), "Should have exactly 1 entry");
    }

    @Test
    void shouldHandleMultipleDifferentDuplicateOperations() {
        // Given - 3 different operations, each duplicated 50 times
        List<ProcessOperationUseCase.ProcessOperationCommand> commands = List.of(
                new ProcessOperationUseCase.ProcessOperationCommand(
                        ExternalReference.of("DUP-A-" + UUID.randomUUID()),
                        OperationType.DEPOSIT,
                        null,
                        accountId,
                        Money.of("10.00"),
                        "test"),
                new ProcessOperationUseCase.ProcessOperationCommand(
                        ExternalReference.of("DUP-B-" + UUID.randomUUID()),
                        OperationType.DEPOSIT,
                        null,
                        accountId,
                        Money.of("20.00"),
                        "test"),
                new ProcessOperationUseCase.ProcessOperationCommand(
                        ExternalReference.of("DUP-C-" + UUID.randomUUID()),
                        OperationType.DEPOSIT,
                        null,
                        accountId,
                        Money.of("30.00"),
                        "test"));

        // When - execute each command 50 times in parallel
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

        List<CompletableFuture<com.ledgerservice.domain.entities.Operation>> futures = commands.stream()
                .flatMap(cmd -> IntStream.range(0, 50)
                        .mapToObj(i -> CompletableFuture.supplyAsync(() -> processOperationUseCase.execute(cmd),
                                executor)))
                .collect(Collectors.toList());

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executor.shutdown();

        // Then - verify only 3 operations were created (one per external_reference)
        long operationCount = operationRepository.count();
        assertEquals(3, operationCount, "Should have exactly 3 operations (one per external_reference)");

        // Verify balance is correct (10 + 20 + 30 = 60)
        var balanceResult = calculateBalanceUseCase.execute(accountId);
        assertEquals(Money.of("60.00"), balanceResult.balance(), "Balance should be 60.00");
        assertEquals(3, balanceResult.entriesCount(), "Should have exactly 3 entries");
    }
}
