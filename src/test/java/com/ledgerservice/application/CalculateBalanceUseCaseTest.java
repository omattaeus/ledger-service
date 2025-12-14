package com.ledgerservice.application;

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

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for CalculateBalanceUseCase
 */
@SpringBootTest
@ActiveProfiles("test")
class CalculateBalanceUseCaseTest {

    @Autowired
    private CalculateBalanceUseCase calculateBalanceUseCase;

    @Autowired
    private ProcessOperationUseCase processOperationUseCase;

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
    void shouldCalculateZeroBalanceForNewAccount() {
        // When
        var result = calculateBalanceUseCase.execute(accountId);

        // Then
        assertNotNull(result);
        assertEquals(accountId, result.accountId());
        assertTrue(result.balance().isZero());
        assertEquals(0, result.entriesCount());
    }

    @Test
    void shouldCalculateBalanceAfterDeposit() {
        // Given - make a deposit
        processOperationUseCase.execute(new ProcessOperationUseCase.ProcessOperationCommand(
                ExternalReference.of("DEP-001"),
                OperationType.DEPOSIT,
                null,
                accountId,
                Money.of("100.00"),
                "test"));

        // When
        var result = calculateBalanceUseCase.execute(accountId);

        // Then
        assertEquals(Money.of("100.00"), result.balance());
        assertEquals(1, result.entriesCount());
    }

    @Test
    void shouldCalculateBalanceAfterMultipleOperations() {
        // Given - multiple deposits
        processOperationUseCase.execute(new ProcessOperationUseCase.ProcessOperationCommand(
                ExternalReference.of("DEP-001"),
                OperationType.DEPOSIT,
                null,
                accountId,
                Money.of("100.00"),
                "test"));

        processOperationUseCase.execute(new ProcessOperationUseCase.ProcessOperationCommand(
                ExternalReference.of("DEP-002"),
                OperationType.DEPOSIT,
                null,
                accountId,
                Money.of("50.00"),
                "test"));

        // When
        var result = calculateBalanceUseCase.execute(accountId);

        // Then
        assertEquals(Money.of("150.00"), result.balance());
        assertEquals(2, result.entriesCount());
    }
}
