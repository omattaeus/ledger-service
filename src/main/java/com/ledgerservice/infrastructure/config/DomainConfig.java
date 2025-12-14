package com.ledgerservice.infrastructure.config;

import com.ledgerservice.domain.services.BalanceCalculator;
import com.ledgerservice.domain.services.EntryFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for domain services
 * Makes pure domain services available as Spring beans
 */
@Configuration
public class DomainConfig {

    @Bean
    public BalanceCalculator balanceCalculator() {
        return new BalanceCalculator();
    }

    @Bean
    public EntryFactory entryFactory() {
        return new EntryFactory();
    }
}