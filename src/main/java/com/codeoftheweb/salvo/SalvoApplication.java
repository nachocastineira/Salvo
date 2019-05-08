package com.codeoftheweb.salvo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.CommandLineRunner;
import java.util.Date;


@SpringBootApplication
public class SalvoApplication {

	public static void main(String[] args) {
		SpringApplication.run(SalvoApplication.class, args);
	}

	@Bean
	public CommandLineRunner initData1(GameRepository gameRepository, PlayerRepository playerRepository, GamePlayerRepository gamePlayerRepository) {
		return (args) -> {
			playerRepository.save(new Player("Jack15", "jackj@mail.com"));
			playerRepository.save(new Player("Luck39", "luckj@mail.com"));
		};
	}

	@Bean
	public CommandLineRunner initData2(GameRepository gameRepository) {
		return (args) -> {

			Date date1 = new Date();
			date1 = Date.from(date1.toInstant());
			gameRepository.save(new Game(date1)); //Fecha al momento de crear objeto

			Date date2 = new Date();
			date2 = Date.from(date2.toInstant().plusSeconds(3600)); //.plusSecond para sumarle 1Hs a la actual
			gameRepository.save(new Game(date2)); //Fecha con 1 hora mas tarde

			Date date3 = new Date();
			date3 = Date.from(date3.toInstant().plusSeconds(7200));
			gameRepository.save(new Game(date3)); //Fecha con 2 horas mas tarde
		};
	}

	@Bean
	public CommandLineRunner initData3(GameRepository gameRepository, PlayerRepository playerRepository, GamePlayerRepository gamePlayerRepository) {
		return (args) -> {
			playerRepository.save(new Player("Jack15", "jackj@mail.com"));
			playerRepository.save(new Player("Luck39", "luckj@mail.com"));

			Date date = new Date();
			date = Date.from(date.toInstant());
			gameRepository.save(new Game(date));
			gameRepository.save(new Game(date));
			gameRepository.save(new Game(date));

			gamePlayerRepository.save(new GamePlayer(new Game(date), new Player("pepe", "pepe@mail.com")));
			gamePlayerRepository.save(new GamePlayer(new Game(date), new Player("juan", "juan@mail.com")));

		};
	}


}
