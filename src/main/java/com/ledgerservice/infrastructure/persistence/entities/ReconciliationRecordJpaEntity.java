package com.ledgerservice.infrastructure.persistence.entities;

import com.ledgerservice.domain.enums.ReconciliationStatus;
import com.ledgerservice.infrastructure.persistence.converters.ReconciliationStatusConverter;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity for Reconciliation Records table
 * Maps domain Reconciliation Record to database
 */
@Entity
@Table(name = "reconciliation_records")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ReconciliationRecordJpaEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "reconciliation_date", nullable = false)
    private LocalDate reconciliationDate;

    @Column(name = "expected_balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal expectedBalance;

    @Column(name = "calculated_balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal calculatedBalance;

    @Column(name = "difference", nullable = false, precision = 19, scale = 4)
    private BigDecimal difference;

    @Column(name = "status", nullable = false, length = 20)
    @Convert(converter = ReconciliationStatusConverter.class)
    private ReconciliationStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}