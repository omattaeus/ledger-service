package com.ledgerservice.infrastructure.persistence.converters;

import com.ledgerservice.domain.enums.OperationStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA Converter for OperationStatus enum to lowercase
 */
@Converter(autoApply = true)
public class OperationStatusConverter implements AttributeConverter<OperationStatus, String> {

    @Override
    public String convertToDatabaseColumn(OperationStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name().toLowerCase();
    }

    @Override
    public OperationStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return OperationStatus.valueOf(dbData.toUpperCase());
    }
}
