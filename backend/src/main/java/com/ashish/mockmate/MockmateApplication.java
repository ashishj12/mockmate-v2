package com.ashish.mockmate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class MockmateApplication {

	public static void main(String[] args) {

		// Load environment file
		Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

		// Set system properties BEFORE Spring starts
		dotenv.entries().forEach(entry -> {
			System.setProperty(entry.getKey(), entry.getValue());
		});

		SpringApplication.run(MockmateApplication.class, args);
	}
}