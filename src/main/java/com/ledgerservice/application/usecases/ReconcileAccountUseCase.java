package com.ledgerservice.application.usecases;

import com.ledgerservice.domain.entities.Entry;
import com.ledgerservice.domain.entities.ReconciliationRecord;
import com.ledgerservice.domain.exceptions.AccountNotFoundException;
import com.ledgerservice.domain.services.BalanceCalculator;
import com.ledgerservice.domain.valueobjects.Money;
import com.ledgerservice.infrastructure.persistence.mappers.EntityMapper;
import com.ledgerservice.infrastructure.persistence.repositories.AccountJpaRepository;
import com.ledgerservice.infrastructure.persistence.repositories.EntryJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Use Case: Reconcile account balance
 * 
 * GUARANTEES:
 * - Read-only for financial data (does NOT auto-correct)
 * - Creates audit record of reconciliation
 * - Detects divergences between expected and calculated balance
 */
@Service
public class ReconcileAccountUseCase {

    private final AccountJpaRepository accountRepository;
    private final EntryJpaRepository entryRepository;
    private final BalanceCalculator balanceCalculator;

    public ReconcileAccountUseCase(
            AccountJpaRepository accountRepository,
            EntryJpaRepository entryRepository,
            BalanceCalculator balanceCalculator) {
        this.accountRepository = accountRepository;
        this.entryRepository = entryRepository;
        this.balanceCalculator = balanceCalculator;
    }

    @Transactional(readOnly = true)
    public ReconciliationResult execute(UUID accountId, Money expectedBalance) {
        // 1. Validate account exists
        accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        // 2. Load all entries and calculate balance
        var entryJpaList = entryRepository.findByAccountIdOrderByCreatedAtAsc(accountId);
        List<Entry> entries = entryJpaList.stream()
                .map(EntityMapper::toDomain)
                .collect(Collectors.toList());

        Money calculatedBalance = balanceCalculator.calculateBalance(entries);

        // 3. Create reconciliation record (domain logic)
        ReconciliationRecord record = ReconciliationRecord.create(
                accountId,
                expectedBalance,
                calculatedBalance);

        // 4. Return result (not persisting for now - will be added in Phase 6)
        return new ReconciliationResult(
                record.getAccountId(),
                record.getExpectedBalance(),
                record.getCalculatedBalance(),
                record.getDifference(),
                record.getStatus(),
                record.isMatch(),
                record.isMismatch());
    }

    /**
     * Result object for reconciliation
     */
    public record ReconciliationResult(
            UUID accountId,
            Money expectedBalance,
            Money calculatedBalance,
            Money difference,
            com.ledgerservice.domain.enums.ReconciliationStatus status,
            boolean isMatch,
            boolean isMismatch) {
    }
}
