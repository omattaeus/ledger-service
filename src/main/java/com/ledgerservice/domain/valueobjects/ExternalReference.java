package com.ledgerservice.domain.valueobjects;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value Object representing an external reference (idempotency key)
 * Immutable and self-validating
 */
public final class ExternalReference {

    private static final int MIN_LENGTH = 1;
    private static final int MAX_LENGTH = 255;
    private static final Pattern VALID_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]+$");

    private final String value;

    private ExternalReference(String value) {
        this.value = value;
    }

    /**
     * Creates ExternalReference with validation
     */
    public static ExternalReference of(String value) {
        Objects.requireNonNull(value, "External reference cannot be null");

        String trimmed = value.trim();

        if (trimmed.isEmpty())
            throw new IllegalArgumentException("External reference cannot be empty");

        if (trimmed.length() < MIN_LENGTH || trimmed.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                    String.format("External reference must be between %d and %d characters",
                            MIN_LENGTH, MAX_LENGTH));
        }

        if (!VALID_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException(
                    "External reference can only contain alphanumeric characters, hyphens, and underscores");
        }

        return new ExternalReference(trimmed);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ExternalReference that = (ExternalReference) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}