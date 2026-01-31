package com.github.rjhoppe.root_v2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.github.rjhoppe.root_v2.utils.Wordnik;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public Wordnik wordnikClient() {
		Dotenv dotenv = Dotenv.load();
		String apiKey = dotenv.get("API_KEY");

		if (apiKey == null || apiKey.trim().isEmpty()) {
      System.err.println("API_KEY not found in .env file.");
		}

		Wordnik wordnikClient = new Wordnik(apiKey);
		try {
			wordnikClient.validate();
		} catch (Exception e) {
			System.err.println("Failed to validate Wordnik API key: " + e.getMessage());
			throw new RuntimeException("Wordnik API key validation failed");
		}
		
		return wordnikClient;
	}
}
