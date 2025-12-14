package com.ledgerservice.infrastructure.persistence.converters;

import com.ledgerservice.domain.enums.ReconciliationStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA Converter for ReconciliationStatus enum to lowercase
 */
@Converter(autoApply = true)
public class ReconciliationStatusConverter implements AttributeConverter<ReconciliationStatus, String> {

    @Override
    public String convertToDatabaseColumn(ReconciliationStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name().toLowerCase();
    }

    @Override
    public ReconciliationStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return ReconciliationStatus.valueOf(dbData.toUpperCase());
    }
}
