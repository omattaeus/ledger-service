package com.ledgerservice.infrastructure.persistence.converters;

import com.ledgerservice.domain.enums.AccountType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA Converter for AccountType enum to lowercase
 */
@Converter(autoApply = true)
public class AccountTypeConverter implements AttributeConverter<AccountType, String> {

    @Override
    public String convertToDatabaseColumn(AccountType attribute) {
        if (attribute == null)
            return null;
        return attribute.name().toLowerCase();
    }

    @Override
    public AccountType convertToEntityAttribute(String dbData) {
        if (dbData == null)
            return null;
        return AccountType.valueOf(dbData.toUpperCase());
    }
}