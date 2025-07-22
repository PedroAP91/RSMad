package com.rsmad.backend;

import org.springframework.boot.SpringApplication;

public class TestRsmadBackendApplication {

	public static void main(String[] args) {
		SpringApplication.from(RsmadBackendApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
