package com.ledgerservice.domain.valueobjects;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MoneyTest {

    @Test
    void shouldCreateMoneyWithValue() {
        Money money = Money.of(new BigDecimal("100.50"));

        assertEquals(new BigDecimal("100.5000"), money.getValue());
        assertEquals("BRL", money.getCurrency().getCurrencyCode());
    }

    @Test
    void shouldCreateMoneyFromString() {
        Money money = Money.of("100.50");

        assertEquals(new BigDecimal("100.5000"), money.getValue());
    }

    @Test
    void shouldCreateZeroMoney() {
        Money money = Money.zero();

        assertTrue(money.isZero());
        assertFalse(money.isPositive());
        assertFalse(money.isNegative());
    }

    @Test
    void shouldDetectPositiveMoney() {
        Money money = Money.of("100");

        assertTrue(money.isPositive());
        assertFalse(money.isZero());
        assertFalse(money.isNegative());
    }

    @Test
    void shouldDetectNegativeMoney() {
        Money money = Money.of("-100");

        assertTrue(money.isNegative());
        assertFalse(money.isZero());
        assertFalse(money.isPositive());
    }

    @Test
    void shouldAddMoney() {
        Money a = Money.of("100.50");
        Money b = Money.of("50.25");

        Money result = a.add(b);

        assertEquals(new BigDecimal("150.7500"), result.getValue());
    }

    @Test
    void shouldSubtractMoney() {
        Money a = Money.of("100.50");
        Money b = Money.of("30.25");

        Money result = a.subtract(b);

        assertEquals(new BigDecimal("70.2500"), result.getValue());
    }

    @Test
    void shouldNegateMoney() {
        Money money = Money.of("100");
        Money negated = money.negate();

        assertEquals(new BigDecimal("-100.0000"), negated.getValue());
        assertEquals(new BigDecimal("100.0000"), negated.negate().getValue());
    }

    @Test
    void shouldCalculateAbsoluteValue() {
        Money negative = Money.of("-100");
        Money positive = negative.abs();

        assertEquals(new BigDecimal("100.0000"), positive.getValue());
        assertTrue(positive.isPositive());
    }

    @Test
    void shouldBeImmutable() {
        Money original = Money.of("100");
        Money added = original.add(Money.of("50"));

        // Original should not change
        assertEquals(new BigDecimal("100.0000"), original.getValue());
        assertEquals(new BigDecimal("150.0000"), added.getValue());
    }

    @Test
    void shouldThrowExceptionWhenAddingDifferentCurrencies() {
        Money brl = Money.of(new BigDecimal("100"), java.util.Currency.getInstance("BRL"));
        Money usd = Money.of(new BigDecimal("100"), java.util.Currency.getInstance("USD"));

        assertThrows(IllegalArgumentException.class, () -> brl.add(usd));
    }

    @Test
    void shouldThrowExceptionWhenValueIsNull() {
        assertThrows(NullPointerException.class, () -> Money.of((BigDecimal) null));
    }

    @Test
    void shouldBeEqualWhenSameValue() {
        Money a = Money.of("100.50");
        Money b = Money.of("100.50");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenDifferentValue() {
        Money a = Money.of("100.50");
        Money b = Money.of("200.00");

        assertNotEquals(a, b);
    }

    @Test
    void shouldHaveReadableToString() {
        Money money = Money.of("100.50");
        String string = money.toString();

        assertTrue(string.contains("100.5"));
        assertTrue(string.contains("BRL"));
    }
}
