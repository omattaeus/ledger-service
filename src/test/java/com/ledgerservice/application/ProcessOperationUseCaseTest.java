package com.ledgerservice.application;

import com.ledgerservice.application.usecases.ProcessOperationUseCase;
import com.ledgerservice.domain.entities.Account;
import com.ledgerservice.domain.entities.Operation;
import com.ledgerservice.domain.enums.AccountType;
import com.ledgerservice.domain.enums.OperationType;
import com.ledgerservice.domain.valueobjects.ExternalReference;
import com.ledgerservice.domain.valueobjects.Money;
import com.ledgerservice.infrastructure.persistence.entities.AccountJpaEntity;
import com.ledgerservice.infrastructure.persistence.mappers.EntityMapper;
import com.ledgerservice.infrastructure.persistence.repositories.AccountJpaRepository;
import com.ledgerservice.infrastructure.persistence.repositories.EntryJpaRepository;
import com.ledgerservice.infrastructure.persistence.repositories.OperationJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for ProcessOperationUseCase
 * Uses Testcontainers for PostgreSQL
 */
@SpringBootTest
@ActiveProfiles("test")
class ProcessOperationUseCaseTest {

    @Autowired
    private ProcessOperationUseCase processOperationUseCase;

    @Autowired
    private AccountJpaRepository accountRepository;

    @Autowired
    private OperationJpaRepository operationRepository;

    @Autowired
    private EntryJpaRepository entryRepository;

    private UUID sourceAccountId;
    private UUID targetAccountId;

    @BeforeEach
    void setUp() {
        // Clean database
        entryRepository.deleteAll();
        operationRepository.deleteAll();
        accountRepository.deleteAll();

        // Create test accounts
        Account sourceAccount = Account.create(AccountType.USER);
        Account targetAccount = Account.create(AccountType.USER);

        AccountJpaEntity sourceJpa = EntityMapper.toJpa(sourceAccount);
        AccountJpaEntity targetJpa = EntityMapper.toJpa(targetAccount);

        accountRepository.save(sourceJpa);
        accountRepository.save(targetJpa);

        sourceAccountId = sourceAccount.getId();
        targetAccountId = targetAccount.getId();
    }

    @Test
    void shouldProcessDepositOperation() {
        // Given
        var command = new ProcessOperationUseCase.ProcessOperationCommand(
                ExternalReference.of("DEP-001"),
                OperationType.DEPOSIT,
                null,
                targetAccountId,
                Money.of("100.00"),
                "test");

        // When
        Operation result = processOperationUseCase.execute(command);

        // Then
        assertNotNull(result);
        assertTrue(result.isProcessed());
        assertEquals(OperationType.DEPOSIT, result.getType());

        // Verify entries were created
        var entries = entryRepository.findByOperationId(result.getId());
        assertEquals(1, entries.size()); // Deposit creates 1 entry (credit)
    }

    @Test
    void shouldProcessTransferOperation() {
        // Given
        var command = new ProcessOperationUseCase.ProcessOperationCommand(
                ExternalReference.of("TRF-001"),
                OperationType.TRANSFER,
                sourceAccountId,
                targetAccountId,
                Money.of("50.00"),
                "test");

        // When
        Operation result = processOperationUseCase.execute(command);

        // Then
        assertNotNull(result);
        assertTrue(result.isProcessed());

        // Verify double-entry: 2 entries (debit + credit)
        var entries = entryRepository.findByOperationId(result.getId());
        assertEquals(2, entries.size());
    }

    @Test
    void shouldBeIdempotent() {
        // Given
        var command = new ProcessOperationUseCase.ProcessOperationCommand(
                ExternalReference.of("IDEM-001"),
                OperationType.DEPOSIT,
                null,
                targetAccountId,
                Money.of("100.00"),
                "test");

        // When - execute twice with same external reference
        Operation first = processOperationUseCase.execute(command);
        Operation second = processOperationUseCase.execute(command);

        // Then - should return same operation
        assertEquals(first.getId(), second.getId());
        assertEquals(first.getExternalReference(), second.getExternalReference());

        // Verify only one operation was created
        long count = operationRepository.count();
        assertEquals(1, count);
    }
}
