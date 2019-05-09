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

/*	@Bean
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
	}*/

	@Bean
	public CommandLineRunner initData3
			(GamePlayerRepository gamePlayerRepository, GameRepository gameRepository, PlayerRepository playerRepository, ShipRepository shipRepository) {
		return (args) -> {

			Date date = new Date();
			date = Date.from(date.toInstant());
			Date date2 = new Date();
			date2 = Date.from(date2.toInstant().plusSeconds(6000));

			Game game1 = new Game();
			game1.setCreationDate(date);
			Game game2 = new Game();
			game2.setCreationDate(date2);
			Game game3 = new Game();
			game3.setCreationDate(date);
			gameRepository.save(game1);
			gameRepository.save(game2);
			gameRepository.save(game3);

			Player player1 = new Player();
			player1.setEmail("lucas@mail.com");
			Player player2 = new Player();
			player2.setEmail("jorge@mail.com");
			Player player3 = new Player();
			player3.setEmail("jack@mail.com");
			playerRepository.save(player1);
			playerRepository.save(player2);
			playerRepository.save(player3);


			GamePlayer gamePlayerTest = new GamePlayer(game1, player1);
			gamePlayerRepository.save(gamePlayerTest);

			Ship ship1 = new Ship();
			ship1.setShipType("cruiser");
			ship1.setGamePlayerID(gamePlayerTest);
			shipRepository.save(ship1);


			gamePlayerRepository.save(new GamePlayer(game1, player1));
			gamePlayerRepository.save(new GamePlayer(game1, player3));
			gamePlayerRepository.save(new GamePlayer(game2, player1));
			gamePlayerRepository.save(new GamePlayer(game2, player2));
			gamePlayerRepository.save(new GamePlayer(game3, player2));
			gamePlayerRepository.save(new GamePlayer(game3, player3));
		};
	}
}
