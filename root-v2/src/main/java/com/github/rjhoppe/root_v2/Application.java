package com.github.rjhoppe.root_v2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.github.rjhoppe.root_v2.game.Game;
import com.github.rjhoppe.root_v2.utils.Wordnik;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);

		Dotenv dotenv = Dotenv.load();
		String apiKey = dotenv.get("API_KEY");

		Wordnik wordnikClient = new Wordnik(apiKey);
		try {
			wordnikClient.validate();
		} catch (Exception e) {
			System.err.println("Failed to validate Wordnik API key: " + e.getMessage());
			System.exit(1);
		}
		
		Game game = new Game("Player 1", wordnikClient);
	}
}
