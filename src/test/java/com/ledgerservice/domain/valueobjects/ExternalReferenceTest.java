package com.ledgerservice.domain.valueobjects;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExternalReferenceTest {

    @Test
    void shouldCreateValidExternalReference() {
        ExternalReference ref = ExternalReference.of("PSP-123-ABC");

        assertEquals("PSP-123-ABC", ref.getValue());
    }

    @Test
    void shouldAcceptAlphanumericWithHyphensAndUnderscores() {
        ExternalReference ref = ExternalReference.of("external_ref-123_ABC");

        assertEquals("external_ref-123_ABC", ref.getValue());
    }

    @Test
    void shouldTrimWhitespace() {
        ExternalReference ref = ExternalReference.of("  PSP-123  ");

        assertEquals("PSP-123", ref.getValue());
    }

    @Test
    void shouldThrowExceptionWhenNull() {
        assertThrows(NullPointerException.class, () -> ExternalReference.of(null));
    }

    @Test
    void shouldThrowExceptionWhenEmpty() {
        assertThrows(IllegalArgumentException.class, () -> ExternalReference.of(""));
    }

    @Test
    void shouldThrowExceptionWhenOnlyWhitespace() {
        assertThrows(IllegalArgumentException.class, () -> ExternalReference.of("   "));
    }

    @Test
    void shouldThrowExceptionWhenContainsInvalidCharacters() {
        assertThrows(IllegalArgumentException.class, () -> ExternalReference.of("PSP@123"));
        assertThrows(IllegalArgumentException.class, () -> ExternalReference.of("PSP#123"));
        assertThrows(IllegalArgumentException.class, () -> ExternalReference.of("PSP 123")); // space
        assertThrows(IllegalArgumentException.class, () -> ExternalReference.of("PSP.123")); // dot
    }

    @Test
    void shouldThrowExceptionWhenTooLong() {
        String longString = "A".repeat(256);
        assertThrows(IllegalArgumentException.class, () -> ExternalReference.of(longString));
    }

    @Test
    void shouldBeEqualWhenSameValue() {
        ExternalReference a = ExternalReference.of("PSP-123");
        ExternalReference b = ExternalReference.of("PSP-123");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenDifferentValue() {
        ExternalReference a = ExternalReference.of("PSP-123");
        ExternalReference b = ExternalReference.of("PSP-456");

        assertNotEquals(a, b);
    }

    @Test
    void shouldHaveReadableToString() {
        ExternalReference ref = ExternalReference.of("PSP-123");

        assertEquals("PSP-123", ref.toString());
    }
}
