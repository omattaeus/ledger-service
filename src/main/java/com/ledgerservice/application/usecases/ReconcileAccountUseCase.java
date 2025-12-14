package com.ledgerservice.application.usecases;

import com.ledgerservice.domain.entities.Entry;
import com.ledgerservice.domain.entities.ReconciliationRecord;
import com.ledgerservice.domain.exceptions.AccountNotFoundException;
import com.ledgerservice.domain.services.BalanceCalculator;
import com.ledgerservice.domain.valueobjects.Money;
import com.ledgerservice.infrastructure.observability.StructuredLogger;
import com.ledgerservice.infrastructure.persistence.mappers.EntityMapper;
import com.ledgerservice.infrastructure.persistence.repositories.AccountJpaRepository;
import com.ledgerservice.infrastructure.persistence.repositories.EntryJpaRepository;
import com.ledgerservice.infrastructure.persistence.repositories.ReconciliationRecordJpaRepository;
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
        private final ReconciliationRecordJpaRepository reconciliationRepository;
        private final StructuredLogger structuredLogger;

        public ReconcileAccountUseCase(
                        AccountJpaRepository accountRepository,
                        EntryJpaRepository entryRepository,
                        BalanceCalculator balanceCalculator,
                        ReconciliationRecordJpaRepository reconciliationRepository,
                        StructuredLogger structuredLogger) {
                this.accountRepository = accountRepository;
                this.entryRepository = entryRepository;
                this.balanceCalculator = balanceCalculator;
                this.reconciliationRepository = reconciliationRepository;
                this.structuredLogger = structuredLogger;
        }

        @Transactional
        public ReconciliationResult execute(UUID accountId, Money expectedBalance) {
                accountRepository.findById(accountId)
                                .orElseThrow(() -> new AccountNotFoundException(accountId));

                var entryJpaList = entryRepository.findByAccountIdOrderByCreatedAtAsc(accountId);
                List<Entry> entries = entryJpaList.stream()
                                .map(EntityMapper::toDomain)
                                .collect(Collectors.toList());

                Money calculatedBalance = balanceCalculator.calculateBalance(entries);

                ReconciliationRecord reconciliation = ReconciliationRecord.create(
                                accountId,
                                expectedBalance,
                                calculatedBalance);

                if (reconciliation.isMismatch()) {
                        structuredLogger.logReconciliationMismatch(
                                        accountId,
                                        expectedBalance.getValue(),
                                        calculatedBalance.getValue(),
                                        reconciliation.getDifference().getValue());
                } else {
                        structuredLogger.logReconciliationMatch(
                                        accountId,
                                        calculatedBalance.getValue());
                }

                var reconciliationJpa = EntityMapper.toJpa(reconciliation);
                reconciliationRepository.save(reconciliationJpa);

                return new ReconciliationResult(
                                reconciliation.getAccountId(),
                                reconciliation.getExpectedBalance(),
                                reconciliation.getCalculatedBalance(),
                                reconciliation.getDifference(),
                                reconciliation.getStatus(),
                                reconciliation.isMatch(),
                                reconciliation.isMismatch());
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
