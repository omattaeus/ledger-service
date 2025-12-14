package com.ledgerservice.infrastructure.persistence.repositories;

import com.ledgerservice.infrastructure.persistence.entities.ReconciliationRecordJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * JPA Repository for Reconciliation Records
 */
@Repository
public interface ReconciliationRecordJpaRepository extends JpaRepository<ReconciliationRecordJpaEntity, UUID> {

    /**
     * Find all reconciliation records for an account, ordered by creation date
     * descending
     */
    List<ReconciliationRecordJpaEntity> findByAccountIdOrderByCreatedAtDesc(UUID accountId);
}
