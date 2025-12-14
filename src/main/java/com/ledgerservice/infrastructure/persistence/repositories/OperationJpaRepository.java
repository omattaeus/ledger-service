package com.ledgerservice.infrastructure.persistence.repositories;

import com.ledgerservice.infrastructure.persistence.entities.OperationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository for Operation
 */
@Repository
public interface OperationJpaRepository extends JpaRepository<OperationJpaEntity, UUID> {

    /**
     * Finds operation by external reference (idempotency check)
     */
    Optional<OperationJpaEntity> findByExternalReference(String externalReference);

    /**
     * Checks if operation exists by external reference
     */
    boolean existsByExternalReference(String externalReference);
}