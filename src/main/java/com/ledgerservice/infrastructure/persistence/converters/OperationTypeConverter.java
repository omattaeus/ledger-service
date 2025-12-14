package com.ledgerservice.infrastructure.persistence.converters;

import com.ledgerservice.domain.enums.OperationType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA Converter for OperationType enum to lowercase
 */
@Converter(autoApply = true)
public class OperationTypeConverter implements AttributeConverter<OperationType, String> {

    @Override
    public String convertToDatabaseColumn(OperationType attribute) {
        if (attribute == null)
            return null;
        return attribute.name().toLowerCase();
    }

    @Override
    public OperationType convertToEntityAttribute(String dbData) {
        if (dbData == null)
            return null;
        return OperationType.valueOf(dbData.toUpperCase());
    }
}
