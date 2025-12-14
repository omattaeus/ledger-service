package com.ledgerservice.application.usecases;

import com.ledgerservice.domain.entities.Entry;
import com.ledgerservice.domain.exceptions.AccountNotFoundException;
import com.ledgerservice.domain.services.BalanceCalculator;
import com.ledgerservice.domain.valueobjects.Money;
import com.ledgerservice.infrastructure.persistence.mappers.EntityMapper;
import com.ledgerservice.infrastructure.persistence.repositories.AccountJpaRepository;
import com.ledgerservice.infrastructure.persistence.repositories.EntryJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Use Case: Calculate account balance
 * 
 * GUARANTEES:
 * - Always recalculates from entries (no cached balance)
 * - Auditable (returns entries used in calculation)
 * - Read-only transaction
 */
@Service
public class CalculateBalanceUseCase {

    private final AccountJpaRepository accountRepository;
    private final EntryJpaRepository entryRepository;
    private final BalanceCalculator balanceCalculator;

    public CalculateBalanceUseCase(
            AccountJpaRepository accountRepository,
            EntryJpaRepository entryRepository,
            BalanceCalculator balanceCalculator) {
        this.accountRepository = accountRepository;
        this.entryRepository = entryRepository;
        this.balanceCalculator = balanceCalculator;
    }

    @Transactional(readOnly = true)
    public BalanceResult execute(UUID accountId) {
        // 1. Validate account exists
        accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        // 2. Load all entries for account
        var entryJpaList = entryRepository.findByAccountIdOrderByCreatedAtAsc(accountId);

        // 3. Convert to domain entries
        List<Entry> entries = entryJpaList.stream()
                .map(EntityMapper::toDomain)
                .collect(Collectors.toList());

        // 4. Calculate balance
        Money balance = balanceCalculator.calculateBalance(entries);
        long entryCount = balanceCalculator.countEntries(entries);

        return new BalanceResult(
                accountId,
                balance,
                entryCount,
                LocalDateTime.now());
    }

    /**
     * Result object containing balance calculation details
     */
    public record BalanceResult(
            UUID accountId,
            Money balance,
            long entriesCount,
            LocalDateTime calculatedAt) {
    }
}
