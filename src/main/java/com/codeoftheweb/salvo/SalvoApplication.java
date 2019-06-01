package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@SpringBootApplication
public class SalvoApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(SalvoApplication.class, args);
	}

	@Bean
	public CommandLineRunner initData3
			(GamePlayerRepository gamePlayerRepository, GameRepository gameRepository,
			 PlayerRepository playerRepository, ShipRepository shipRepository, SalvoRepository salvoRepository,
			 ScoreRepository scoreRepository) {
		return (args) -> {

			//-- NEW DATES
			Date date = new Date();
			date = Date.from(date.toInstant());

			//-- NEW GAMES
			Game game1 = new Game(date);
			Game game2 = new Game(date);
			Game game3 = new Game(date);
			Game game4 = new Game(date);
			gameRepository.save(game1);
			gameRepository.save(game2);
			gameRepository.save(game3);
			gameRepository.save(game4);


			//-- NEW PLAYERS
			Player player1 = new Player("j.bauer@ctu.gov", passwordEncoder().encode("24"));
			Player player2 = new Player("c.obrian@ctu.gov", passwordEncoder().encode("42"));
			Player player3 = new Player("kim_bauer@gmail.com", passwordEncoder().encode("kb"));
			Player player4 = new Player("t.almeida@ctu.gov", passwordEncoder().encode("mole"));
			playerRepository.save(player1);
			playerRepository.save(player2);
			playerRepository.save(player3);
			playerRepository.save(player4);


			//-- NEW GAMEPLAYERS
			GamePlayer gamePlayer1 = new GamePlayer(game1, player1);
			GamePlayer gamePlayer2 = new GamePlayer(game1, player3);
			GamePlayer gamePlayer3 = new GamePlayer(game2, player1);
			GamePlayer gamePlayer4 = new GamePlayer(game2, player2);
			GamePlayer gamePlayer5 = new GamePlayer(game3, player3);
			GamePlayer gamePlayer6 = new GamePlayer(game3, player2);
			GamePlayer gamePlayer7 = new GamePlayer(game4, player4);
			gamePlayerRepository.save(gamePlayer1);
			gamePlayerRepository.save(gamePlayer2);
			gamePlayerRepository.save(gamePlayer3);
			gamePlayerRepository.save(gamePlayer4);
			gamePlayerRepository.save(gamePlayer5);
			gamePlayerRepository.save(gamePlayer6);
			gamePlayerRepository.save(gamePlayer7);

			//-- NEW LOCATIONS SHIP
			List<String> shipLocations1 = new ArrayList<>();
			shipLocations1.add("F2");
			shipLocations1.add("F3");
			shipLocations1.add("H1");
			shipLocations1.add("B9");
			shipLocations1.add("C7");
			List<String> shipLocations2 = new ArrayList<>();
			shipLocations2.add("F5");
			shipLocations2.add("F6");
			shipLocations2.add("F7");
			shipLocations2.add("C4");
			shipLocations2.add("C5");

			List<String> shipLocations3 = new ArrayList<>();
			shipLocations3.add("B4");
			shipLocations3.add("C4");
			shipLocations3.add("D4");
			List<String> shipLocations4 = new ArrayList<>();
			shipLocations4.add("A10");
			shipLocations4.add("B10");
			shipLocations4.add("C10");

			//-- NEW SHIPS
			String carrier = "carrier";//length = 5
			String battleship = "battleship"; //length = 4
			String submarine = "submarine"; //length = 3
			String destroyer = "destroyer"; //length = 3
			String patrolboat = "patrolboat"; //length = 2

			Ship ship1 = new Ship(gamePlayer1, shipLocations1, battleship); //Game 1 -> Player 1
			Ship ship2 = new Ship(gamePlayer1, shipLocations3, destroyer);  //Game 1 -> Player 1
			Ship ship3 = new Ship(gamePlayer2, shipLocations2, submarine);    //Game 1 -> Player 3
			Ship ship4 = new Ship(gamePlayer2, shipLocations4, patrolboat);    //Game 1 -> Player 3
			shipRepository.save(ship1);
			shipRepository.save(ship2);
			shipRepository.save(ship3);
			shipRepository.save(ship4);

			Ship ship5_test = new Ship(gamePlayer3, shipLocations4, patrolboat);    //Game 1 -> Player 3
			shipRepository.save(ship5_test);


			//-- NEW LOCATIONS SALVOES
			List<String> salvoLocation1 = new ArrayList<>();
			salvoLocation1.add("D4");
			salvoLocation1.add("H2");
			salvoLocation1.add("F5");
			List<String> salvoLocation2 = new ArrayList<>();
			salvoLocation2.add("A10");
			salvoLocation2.add("H4");
			salvoLocation2.add("H2");
			List<String> salvoLocation3 = new ArrayList<>();
			salvoLocation3.add("C10");
			salvoLocation3.add("B5");
			salvoLocation3.add("C7");

			List<String> salvoLocation4 = new ArrayList<>();
			salvoLocation4.add("H3");
			salvoLocation4.add("A1");
			salvoLocation4.add("I5");
			List<String> salvoLocation5 = new ArrayList<>();
			salvoLocation5.add("F6");
			salvoLocation5.add("I10");
			salvoLocation5.add("I8");
			List<String> salvoLocation6 = new ArrayList<>();
			salvoLocation6.add("F10");
			salvoLocation6.add("I1");
			salvoLocation6.add("E6");

			//-- NEW SALVOES
			Salvo salvo1 = new Salvo(gamePlayer1, 1, salvoLocation1);
			Salvo salvo2 = new Salvo(gamePlayer2, 1, salvoLocation4);
			Salvo salvo3 = new Salvo(gamePlayer1, 2, salvoLocation2);
			Salvo salvo4 = new Salvo(gamePlayer2, 2, salvoLocation5);
			Salvo salvo5 = new Salvo(gamePlayer1, 3, salvoLocation3);
			Salvo salvo6 = new Salvo(gamePlayer2, 3, salvoLocation6);
			salvoRepository.save(salvo1);
			salvoRepository.save(salvo2);
			salvoRepository.save(salvo3);
			salvoRepository.save(salvo4);
			salvoRepository.save(salvo5);
			salvoRepository.save(salvo6);

			//-- NEW SCORES
			float win = 1;
			float tie = (float) 0.5;
			float lose = 0;

			Score score1 = new Score(game1, player1, win, date);
			Score score2 = new Score(game1, player2, lose, date);
			Score score3 = new Score(game2, player2, tie, date);
			Score score4 = new Score(game2, player3, tie, date);
			Score score5 = new Score(game3, player3, win, date);
			Score score6 = new Score(game3, player1, lose, date);
			scoreRepository.save(score1);
			scoreRepository.save(score2);
			scoreRepository.save(score3);
			scoreRepository.save(score4);
			scoreRepository.save(score5);
			scoreRepository.save(score6);
		};
	}
	@Bean
	public PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}
}

							//--------------- SECURITY ---------------//
//-- AUTENTICACION DEL USUARIO
@Configuration
class WebSecurityConfiguration extends GlobalAuthenticationConfigurerAdapter {

	@Autowired
	PasswordEncoder passwordEncoder;

	@Autowired
	PlayerRepository playerRepository;

	@Override
	public void init(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(inputName -> {
			Player player = playerRepository.findByUsername(inputName);
			if (player != null) {
				return new User(player.getUsername(), player.getPassword(),
						AuthorityUtils.createAuthorityList("user"));
			} else {
				throw new UsernameNotFoundException("Unknown player: " + inputName);
			}
		});
	}
}

//-- RUTAS PERMITIDAS, LOGIN Y LOGOUT
@EnableWebSecurity
@Configuration
class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
				.authorizeRequests()
				.antMatchers("/web/games_3.html").permitAll()
				.antMatchers("/web/**").permitAll()
				.antMatchers("/api/players").permitAll()
				.antMatchers("/api/games").permitAll()
				.antMatchers("/api/leaderBoard").permitAll()
				.antMatchers("/api/game_view/*").hasAuthority("user")
				.antMatchers("/rest/").denyAll()
				.anyRequest().permitAll();

		http
				.formLogin()
				.usernameParameter("username")
				.passwordParameter("password")
				.loginPage("/api/login");

		http
				.logout().logoutUrl("/api/logout");

		http.headers().frameOptions().sameOrigin();

		// turn off checking for CSRF tokens
		http.csrf().disable();
		// if user is not authenticated, just send an authentication failure response
		http.exceptionHandling().authenticationEntryPoint((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));
		// if login is successful, just clear the flags asking for authentication
		http.formLogin().successHandler((req, res, auth) -> clearAuthenticationAttributes(req));
		// if login fails, just send an authentication failure response
		http.formLogin().failureHandler((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));
		// if logout is successful, just send a success response
		http.logout().logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler());
	}

	private void clearAuthenticationAttributes(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
		}
	}
}

