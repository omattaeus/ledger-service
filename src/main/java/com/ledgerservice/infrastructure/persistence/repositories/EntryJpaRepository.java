package com.ledgerservice.infrastructure.persistence.repositories;

import com.ledgerservice.infrastructure.persistence.entities.EntryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA Repository for Entry
 */
@Repository
public interface EntryJpaRepository extends JpaRepository<EntryJpaEntity, UUID> {

    /**
     * Finds all entries for an account (for balance calculation)
     * Ordered by creation date to maintain consistent ordering
     */
    List<EntryJpaEntity> findByAccountIdOrderByCreatedAtAsc(UUID accountId);

    /**
     * Finds all entries for an operation
     */
    List<EntryJpaEntity> findByOperationId(UUID operationId);

    /**
     * Counts entries for an account
     */
    long countByAccountId(UUID accountId);
}