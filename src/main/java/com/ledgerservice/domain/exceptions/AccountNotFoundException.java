package com.ledgerservice.domain.exceptions;

import java.util.UUID;

/**
 * Thrown when an account is not found
 */
public class AccountNotFoundException extends DomainException {

    public AccountNotFoundException(UUID accountId) {
        super(String.format("Account not found: %s", accountId));
    }
}
