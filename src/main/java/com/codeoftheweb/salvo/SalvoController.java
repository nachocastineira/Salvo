package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api")
public class SalvoController {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @RequestMapping("/games")
    public Map<String, Object> makeLoggedPlayer(Authentication authentication){
        Map<String, Object> dto = new LinkedHashMap<>();
        authentication = SecurityContextHolder.getContext().getAuthentication();
        Player authenticatedPlayer = getAuthentication(authentication);
        if(authenticatedPlayer == null) {
            dto.put("player", "Guest");
        }
        else {
            dto.put("player", loggedPlayerDTO(authenticatedPlayer));
        }
        dto.put("games", getGames());

        return dto;
    }

    private Player getAuthentication(Authentication authentication) {
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken){
            return null;
        }
        else {
            return (playerRepository.findByUsername(authentication.getName()));
        }
    }

    public  List<Object> getGames(){
        return gameRepository
                .findAll()
                .stream()
                .map(game -> gameDTO(game)) //devuelvo un solo map con dos registros {1 juego -> 2 jugadores}
                .collect(Collectors.toList());
    }

    public Map<String, Object> gameDTO(Game game) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", game.getId());
        dto.put("created", game.getCreated().getTime());
        dto.put("gamePlayers", getGamePlayersLista(game.getGamePlayers())); //obtengo todos los jugadores de ese juego, le mando los gamePlayer de ese juego
        dto.put("scores", getScoreLista(game.getScores()));
        return dto;
    }

        public List<Object> getScoreLista(Set<Score> scores){
        return scores.stream().map(score -> scoreDTO(score)).collect(Collectors.toList());
        }

    //hago lo mismo que arriba pero itero los gamePlayers
    public List<Object> getGamePlayersLista(Set<GamePlayer> gamePlayers){
        return gamePlayers
                .stream()
                .map(gamePlayer -> gamePlayerDTO(gamePlayer)) //itero, para ir obteniendo todos los gamePlayer con el metodo de abajo
                .collect(Collectors.toList());
    }

    public Map<String, Object> gamePlayerDTO(GamePlayer gamePlayer) { //el map recibe un gamePlayer, y devuelvo sus datos
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", gamePlayer.getId());
        dto.put("player", gamePlayer.getPlayer());
        return dto;
    }


    private Map<String,Object> shipDTO(Ship ship){
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("shipType", ship.getType());
        dto.put("shipLocations", ship.getLocations());
//        dto.put("player", ship.getGamePlayer().getPlayer().getId());
        return dto;
    }

    private List<Map<String, Object>> getShipList(List<Ship> ships) {
        { return ships
                .stream()
                .map(ship -> shipDTO(ship))
                .collect(Collectors.toList());
        }
    }

    public Map<String, Object> salvoDTO(Salvo salvo){
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
//        dto.put("id", salvo.getId());
        dto.put("turn", salvo.getTurn());
        dto.put("player", salvo.getGamePlayer().getPlayer().getId());
        dto.put("locations", salvo.getLocations());
        return dto;
    }

    private List<Map<String, Object>> makeSalvoList(List<Salvo> salvoes) {
        return salvoes
                .stream()
                .map(salvo -> salvoDTO(salvo))
                .collect(Collectors.toList());
    }


    public Map<String, Object> scoreDTO(Score score){
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("playerID", score.getPlayer().getId());
        dto.put("score", score.getScore());
        dto.put("finishDate", score.getFinishDate());
        return dto;
    }

    private List<Map<String,Object>> getSalvoList(Game game) {
        List<Map<String,Object>> myList = new ArrayList<>();
        game.getGamePlayers().forEach(gamePlayer -> myList.addAll(makeSalvoList(gamePlayer.getSalvoes())));
        return myList;
    }


        @Autowired
    private GamePlayerRepository gamePlayerRepository;


    @RequestMapping("/game_view/{id}")
    public Map<String, Object> getGameView(@PathVariable long id){
        return gameViewDTO(gamePlayerRepository.findById(id).get());
    }

    private Map<String,Object> gameViewDTO(GamePlayer gamePlayer){
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", gamePlayer.getGame().getId());
        dto.put("created", gamePlayer.getGame().getCreated());
        dto.put("gamePlayers", getGamePlayersLista(gamePlayer.getGame().getGamePlayers()));
        dto.put("ships", gamePlayer.getShips());
        dto.put("salvoes", gamePlayer.getGame().getGamePlayers().stream()
                .flatMap(gamePlayer1 -> gamePlayer1.getSalvoes().stream().map(salvo -> salvoDTO (salvo))));
        dto.put("scores", gamePlayer.getPlayer().getScores().stream().map(score -> scoreDTO(score)));

        return dto;
    }


    private Map<String, Object> playerDTO(Player player){
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", player.getId());
        dto.put("username", player.getUsername());
        dto.put("score", getScoreList(player));
        return dto;
    }

    private Map<String, Object> loggedPlayerDTO(Player player){
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", player.getId());
        dto.put("name", player.getUsername());
        return dto;
    }


    @Autowired
    PlayerRepository playerRepo;
    @RequestMapping("/leaderBoard")
    public List<Object> getAllScores() {
        return playerRepo
                .findAll()
                .stream()
                .map(player -> playerDTO(player))
                .collect(Collectors.toList());
    }


    private Map<String, Object> playerLeaderBoardDTO(Player player) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", player.getId());
        dto.put("name", player.getUsername());
        dto.put("score", getScoreList(player));
        return dto;
    }

    private Map<String, Object> getScoreList(Player player) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("name", player.getUsername());
        dto.put("total", player.getScoreTotal(player));
        dto.put("won", player.getWins(player.getScores()));
        dto.put("lost", player.getLoses(player.getScores()));
        dto.put("tied", player.getDraws(player.getScores()));
        return dto;
    }

    @RequestMapping(path = "/players", method = RequestMethod.POST)
    public ResponseEntity<Object> register (@RequestParam String username, @RequestParam String password){

        if (username.isEmpty() || password.isEmpty()) {
            return new ResponseEntity<>("Missing data", HttpStatus.FORBIDDEN);
        }

        if (playerRepository.findByUsername(username) !=  null) {
            return new ResponseEntity<>("El username ("+username+") se encuentra ocupado.", HttpStatus.FORBIDDEN);
        }
        playerRepository.save(new Player(username,  passwordEncoder.encode(password)));

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    //crea nuevo game con gamePlayer, se le asigna al usuario logueado --> (variables de sesion)
    @RequestMapping(path = "/games", method = RequestMethod.POST)
    public ResponseEntity<Object> newGame (Authentication authentication){

        authentication = SecurityContextHolder.getContext().getAuthentication();
        Player authenticatedPlayer = getAuthentication(authentication);

        if(authenticatedPlayer == null){
            return new ResponseEntity<>("No name given", HttpStatus.FORBIDDEN);
        }
        else {
            Date date = Date.from(java.time.ZonedDateTime.now().toInstant());

            Game newGame = new Game(date);
            gameRepository.save(newGame);

            GamePlayer newGamePlayer = new GamePlayer(newGame, authenticatedPlayer);
             gamePlayerRepository.save(newGamePlayer);

            return new ResponseEntity<>("Game Created", HttpStatus.CREATED);
        }
    }

    @RequestMapping(path = "game/{id}/players", method = RequestMethod.POST)
    public ResponseEntity<Object> joinGame (Authentication authentication, @PathVariable long id){

        authentication = SecurityContextHolder.getContext().getAuthentication();
        Player authenticatedPlayer = getAuthentication(authentication);

        Game gameActual = gameRepository.findById(id).get();

        if(authenticatedPlayer == null)
            return new ResponseEntity<>("No such player", HttpStatus.UNAUTHORIZED);

        if(gameActual == null)
            return new ResponseEntity<>("No such game", HttpStatus.FORBIDDEN);

        //if para juego lleno (2 players)
/*        if()
            return new ResponseEntity<>("Game is full", HttpStatus.FORBIDDEN);*/


        else {
            GamePlayer newGamePlayer = new GamePlayer(gameActual,authenticatedPlayer);
            gamePlayerRepository.save(newGamePlayer);

            return new ResponseEntity<>("Join Game", HttpStatus.CREATED);
        }
        }
    }



