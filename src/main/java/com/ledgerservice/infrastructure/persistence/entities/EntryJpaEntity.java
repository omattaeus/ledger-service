package com.ledgerservice.infrastructure.persistence.entities;

import com.ledgerservice.domain.enums.Direction;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity for Entry table (double-entry bookkeeping)
 */
@Entity
@Table(name = "entries", indexes = {
        @Index(name = "idx_entries_operation_id", columnList = "operation_id"),
        @Index(name = "idx_entries_account_id", columnList = "account_id"),
        @Index(name = "idx_entries_created_at", columnList = "created_at"),
        @Index(name = "idx_entries_account_created", columnList = "account_id,created_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class EntryJpaEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "operation_id", nullable = false)
    private UUID operationId;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "direction", nullable = false, length = 10)
    private Direction direction;

    @Column(name = "entry_type", nullable = false, length = 50)
    private String entryType;

    @Column(name = "source", nullable = false, length = 50)
    private String source;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}