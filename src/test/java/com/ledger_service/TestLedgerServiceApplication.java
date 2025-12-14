package com.ledger_service;

import org.springframework.boot.SpringApplication;

public class TestLedgerServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(LedgerServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
