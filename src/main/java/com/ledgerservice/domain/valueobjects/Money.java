package com.ledgerservice.domain.valueobjects;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

/**
 * Value Object representing monetary value
 * Immutable and self-validating
 */
public final class Money {

    private static final Currency DEFAULT_CURRENCY = Currency.getInstance("BRL");
    private static final int SCALE = 4; // Match database DECIMAL(19,4)

    private final BigDecimal value;
    private final Currency currency;

    private Money(BigDecimal value, Currency currency) {
        this.value = value.setScale(SCALE, RoundingMode.HALF_UP);
        this.currency = currency;
    }

    /**
     * Creates Money with default currency (BRL)
     */
    public static Money of(BigDecimal value) {
        return of(value, DEFAULT_CURRENCY);
    }

    /**
     * Creates Money with specified currency
     */
    public static Money of(BigDecimal value, Currency currency) {
        Objects.requireNonNull(value, "Value cannot be null");
        Objects.requireNonNull(currency, "Currency cannot be null");
        return new Money(value, currency);
    }

    /**
     * Creates Money from string value
     */
    public static Money of(String value) {
        Objects.requireNonNull(value, "Value cannot be null");
        return of(new BigDecimal(value));
    }

    /**
     * Creates zero money
     */
    public static Money zero() {
        return of(BigDecimal.ZERO);
    }

    /**
     * Checks if this money is positive
     */
    public boolean isPositive() {
        return value.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Checks if this money is negative
     */
    public boolean isNegative() {
        return value.compareTo(BigDecimal.ZERO) < 0;
    }

    /**
     * Checks if this money is zero
     */
    public boolean isZero() {
        return value.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * Adds another money value
     */
    public Money add(Money other) {
        validateSameCurrency(other);
        return new Money(this.value.add(other.value), this.currency);
    }

    /**
     * Subtracts another money value
     */
    public Money subtract(Money other) {
        validateSameCurrency(other);
        return new Money(this.value.subtract(other.value), this.currency);
    }

    /**
     * Returns negated value
     */
    public Money negate() {
        return new Money(this.value.negate(), this.currency);
    }

    /**
     * Returns absolute value
     */
    public Money abs() {
        return new Money(this.value.abs(), this.currency);
    }

    private void validateSameCurrency(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                    String.format("Cannot operate on different currencies: %s vs %s",
                            this.currency.getCurrencyCode(),
                            other.currency.getCurrencyCode()));
        }
    }

    public BigDecimal getValue() {
        return value;
    }

    public Currency getCurrency() {
        return currency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Money money = (Money) o;
        return value.compareTo(money.value) == 0 && currency.equals(money.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, currency);
    }

    @Override
    public String toString() {
        return String.format("%s %s", currency.getCurrencyCode(), value.toPlainString());
    }
}