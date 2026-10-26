package com.kotlonies.api;

import org.springframework.boot.SpringApplication;

public class TestKotloniesApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(KotloniesApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
