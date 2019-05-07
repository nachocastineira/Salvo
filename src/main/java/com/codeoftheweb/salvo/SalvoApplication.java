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


	// CommandLineRunner para crear nuevos Player y Game
	@Bean
	public CommandLineRunner initData(GameRepository gameRepository, PlayerRepository playerRepository, GamePlayerRepository gamePlayerRepository) {
		return (args) -> {
			playerRepository.save(new Player("Jack15", "jackj@mail.com"));
			playerRepository.save(new Player("Luck39", "luckj@mail.com"));

			//Faltan agregar las fechas
			gameRepository.save(new Game()); //Fecha al momento de crear objeto
			gameRepository.save(new Game()); //Fecha con 1 hora mas tarde
			gameRepository.save(new Game()); //Fecha con 2 horas mas tarde

			gamePlayerRepository.save((new GamePlayer()));

		};
	}


}
