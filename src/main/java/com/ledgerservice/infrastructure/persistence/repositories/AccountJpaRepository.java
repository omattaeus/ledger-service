package com.ledgerservice.infrastructure.persistence.repositories;

import com.ledgerservice.infrastructure.persistence.entities.AccountJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Spring Data JPA Repository for Account
 */
@Repository
public interface AccountJpaRepository extends JpaRepository<AccountJpaEntity, UUID> {
}
