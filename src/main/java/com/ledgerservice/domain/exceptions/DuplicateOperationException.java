package com.ledgerservice.domain.exceptions;

import com.ledgerservice.domain.valueobjects.ExternalReference;

/**
 * Thrown when a duplicate operation is detected
 */
public class DuplicateOperationException extends DomainException {

    public DuplicateOperationException(ExternalReference externalReference) {
        super(String.format("Operation already exists with external reference: %s", externalReference));
    }
}
