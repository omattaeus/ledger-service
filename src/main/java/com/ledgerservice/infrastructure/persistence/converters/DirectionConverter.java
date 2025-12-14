package com.ledgerservice.infrastructure.persistence.converters;

import com.ledgerservice.domain.enums.Direction;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA Converter for Direction enum to lowercase
 */
@Converter(autoApply = true)
public class DirectionConverter implements AttributeConverter<Direction, String> {

    @Override
    public String convertToDatabaseColumn(Direction attribute) {
        if (attribute == null)
            return null;
        return attribute.name().toLowerCase();
    }

    @Override
    public Direction convertToEntityAttribute(String dbData) {
        if (dbData == null)
            return null;
        return Direction.valueOf(dbData.toUpperCase());
    }
}
