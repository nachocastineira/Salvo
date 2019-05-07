package com.codeoftheweb.salvo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.CommandLineRunner;

@SpringBootApplication
public class SalvoApplication {

	public static void main(String[] args) {
		SpringApplication.run(SalvoApplication.class, args);
	}

	// CommandLineRunner para crear nuevos Player
/*	@Bean
	public CommandLineRunner initData(PlayerRepository repository) {
		return (args) -> {
			repository.save(new Player("Jack15", "jackj@mail.com"));
			repository.save(new Player("Luck39", "luckj@mail.com"));

		};
	}*/

	// CommandLineRunner para crear nuevos Game
	@Bean
	public CommandLineRunner initData(GameRepository repository) {
		return (args) -> {
			//Faltan agregar las fechas
			repository.save(new Game()); //Fecha al momento de crear objeto
			repository.save(new Game()); //Fecha con 1 hora mas tarde
			repository.save(new Game()); //Fecha con 2 horas mas tarde
		};
	}

}
