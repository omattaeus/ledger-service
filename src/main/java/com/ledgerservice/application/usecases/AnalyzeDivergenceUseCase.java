package com.ledgerservice.application.usecases;

import com.ledgerservice.domain.entities.ReconciliationRecord;
import com.ledgerservice.infrastructure.persistence.entities.EntryJpaEntity;
import com.ledgerservice.infrastructure.persistence.entities.ReconciliationRecordJpaEntity;
import com.ledgerservice.infrastructure.persistence.mappers.EntityMapper;
import com.ledgerservice.infrastructure.persistence.repositories.EntryJpaRepository;
import com.ledgerservice.infrastructure.persistence.repositories.ReconciliationRecordJpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Use case for analyzing divergences in reconciliation
 * Helps identify where and when a discrepancy might have started
 */
@Service
public class AnalyzeDivergenceUseCase {

        private final ReconciliationRecordJpaRepository reconciliationRepository;
        private final EntryJpaRepository entryRepository;

        public AnalyzeDivergenceUseCase(
                        ReconciliationRecordJpaRepository reconciliationRepository,
                        EntryJpaRepository entryRepository) {
                this.reconciliationRepository = reconciliationRepository;
                this.entryRepository = entryRepository;
        }

        /**
         * Analyzes a divergence by fetching the reconciliation record
         * and the recent entries for the account
         */
        public DivergenceAnalysisResult execute(UUID reconciliationId, int entryLimit) {
                ReconciliationRecordJpaEntity reconciliationJpa = reconciliationRepository.findById(reconciliationId)
                                .orElseThrow(
                                                () -> new IllegalArgumentException("Reconciliation record not found: "
                                                                + reconciliationId));

                ReconciliationRecord reconciliation = EntityMapper.toDomain(reconciliationJpa);

                List<EntryJpaEntity> recentEntriesJpa = entryRepository
                                .findByAccountIdOrderByCreatedAtDesc(reconciliation.getAccountId())
                                .stream()
                                .limit(entryLimit)
                                .toList();

                String analysis = generateAnalysis(reconciliation, recentEntriesJpa.size());

                return new DivergenceAnalysisResult(
                                reconciliation,
                                recentEntriesJpa,
                                analysis);
        }

        private String generateAnalysis(ReconciliationRecord reconciliation, int entryCount) {
                if (reconciliation.isMatch())
                        return "Balances match. No divergence detected.";

                String direction = reconciliation.getDifference().getValue().signum() > 0 ? "higher" : "lower";

                return String.format(
                                "Divergence detected! Calculated balance is %s than expected by %s. " +
                                                "Showing the last %d entries for investigation. " +
                                                "Check for: missing operations, duplicate processing, or incorrect amount calculations.",
                                direction,
                                reconciliation.getDifference().getValue().abs(),
                                entryCount);
        }

        /**
         * Result containing reconciliation record and related entries
         */
        public record DivergenceAnalysisResult(
                        ReconciliationRecord reconciliation,
                        List<EntryJpaEntity> recentEntries,
                        String analysis) {
        }
}