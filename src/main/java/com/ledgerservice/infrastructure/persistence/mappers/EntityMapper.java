package com.ledgerservice.infrastructure.persistence.mappers;

import com.ledgerservice.domain.entities.Account;
import com.ledgerservice.domain.entities.Entry;
import com.ledgerservice.domain.entities.Operation;
import com.ledgerservice.domain.valueobjects.ExternalReference;
import com.ledgerservice.domain.valueobjects.Money;
import com.ledgerservice.infrastructure.persistence.entities.AccountJpaEntity;
import com.ledgerservice.infrastructure.persistence.entities.EntryJpaEntity;
import com.ledgerservice.infrastructure.persistence.entities.OperationJpaEntity;

/**
 * Maps between domain entities and JPA entities
 */
public class EntityMapper {

    // Account mappings

    public static AccountJpaEntity toJpa(Account domain) {
        return new AccountJpaEntity(
                domain.getId(),
                domain.getType(),
                domain.getCreatedAt());
    }

    public static Account toDomain(AccountJpaEntity jpa) {
        return Account.reconstitute(
                jpa.getId(),
                jpa.getType(),
                jpa.getCreatedAt());
    }

    // Operation mappings

    public static OperationJpaEntity toJpa(Operation domain) {
        return new OperationJpaEntity(
                domain.getId(),
                domain.getExternalReference().getValue(),
                domain.getType(),
                domain.getStatus(),
                domain.getCreatedAt(),
                domain.getProcessedAt());
    }

    public static Operation toDomain(OperationJpaEntity jpa) {
        return Operation.reconstitute(
                jpa.getId(),
                ExternalReference.of(jpa.getExternalReference()),
                jpa.getOperationType(),
                jpa.getStatus(),
                jpa.getCreatedAt(),
                jpa.getProcessedAt(),
                null);
    }

    // Entry mappings

    public static EntryJpaEntity toJpa(Entry domain) {
        return new EntryJpaEntity(
                domain.getId(),
                domain.getOperationId(),
                domain.getAccountId(),
                domain.getAmount().getValue(),
                domain.getDirection(),
                domain.getEntryType(),
                domain.getSource(),
                domain.getCreatedAt());
    }

    public static Entry toDomain(EntryJpaEntity jpa) {
        return Entry.reconstitute(
                jpa.getId(),
                jpa.getOperationId(),
                jpa.getAccountId(),
                Money.of(jpa.getAmount()),
                jpa.getDirection(),
                jpa.getEntryType(),
                jpa.getSource(),
                jpa.getCreatedAt());
    }
}