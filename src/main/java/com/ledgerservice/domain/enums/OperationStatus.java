package com.ledgerservice.domain.enums;

/**
 * Status of an operation in its lifecycle
 */
public enum OperationStatus {
    /**
     * Operation is being processed
     */
    PROCESSING,

    /**
     * Operation completed successfully
     */
    PROCESSED,

    /**
     * Operation was ignored (duplicate detected)
     */
    IGNORED,

    /**
     * Operation failed during processing
     */
    FAILED
}
