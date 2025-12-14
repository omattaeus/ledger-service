package com.ledgerservice.infrastructure.persistence.entities;

import com.ledgerservice.domain.enums.OperationStatus;
import com.ledgerservice.domain.enums.OperationType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity for Operation table
 */
@Entity
@Table(name = "operations", indexes = {
        @Index(name = "idx_operations_external_reference", columnList = "external_reference", unique = true),
        @Index(name = "idx_operations_status", columnList = "status"),
        @Index(name = "idx_operations_created_at", columnList = "created_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class OperationJpaEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "external_reference", nullable = false, unique = true, length = 255)
    private String externalReference;

    @Column(name = "operation_type", nullable = false, length = 20)
    private OperationType operationType;

    @Setter
    @Column(name = "status", nullable = false, length = 20)
    private OperationStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Setter
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
}