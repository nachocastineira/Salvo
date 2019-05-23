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
    private GamePlayerRepository gamePlayerRepository;
    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ShipRepository shipRepository;
    @Autowired
    private SalvoRepository salvoRepository;

    @RequestMapping("/games")
    public Map<String, Object> makeLoggedPlayer(Authentication authentication) {
        Map<String, Object> dto = new LinkedHashMap<>();

        Player authenticatedPlayer = getAuthentication(authentication);
        if (authenticatedPlayer == null)
            dto.put("player", "Invitado");
        else
            dto.put("player", loggedPlayerDTO(authenticatedPlayer));

        dto.put("games", getGames());
        return dto;
    }

    private Player getAuthentication(Authentication authentication) {
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken)
            return null;
        else
            return (playerRepository.findByUsername(authentication.getName()));
    }

    public List<Object> getGames() {
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

    public List<Object> getScoreLista(Set<Score> scores) {
        return scores.stream().map(score -> scoreDTO(score)).collect(Collectors.toList());
    }

    //hago lo mismo que arriba pero itero los gamePlayers
    public List<Object> getGamePlayersLista(List<GamePlayer> gamePlayers) {
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


    private Map<String, Object> shipDTO(Ship ship) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("type", ship.getType());
        dto.put("locations", ship.getLocations());
        return dto;
    }

    private List<Map<String, Object>> getShipList(List<Ship> ships) {
        return ships
                .stream()
                .map(ship -> shipDTO(ship))
                .collect(Collectors.toList());
    }

    public Map<String, Object> salvoDTO(Salvo salvo) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
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

    public Map<String, Object> scoreDTO(Score score) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("playerID", score.getPlayer().getId());
        dto.put("score", score.getScore());
        dto.put("finishDate", score.getFinishDate());
        return dto;
    }

    private List<Map<String, Object>> getSalvoList(Game game) {
        List<Map<String, Object>> myList = new ArrayList<>();
        game.getGamePlayers().forEach(gamePlayer -> myList.addAll(makeSalvoList(gamePlayer.getSalvoes())));
        return myList;
    }

    @RequestMapping("/game_view/{id}")
    public ResponseEntity<Map<String, Object>> getGameView(@PathVariable long id, Authentication authentication) {

        Player authenticatedPlayer = getAuthentication(authentication);
        GamePlayer gamePlayer = gamePlayerRepository.findById(id).orElse(null);

            if (wrongGamePlayer(gamePlayer, authenticatedPlayer))
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            else
            return new ResponseEntity<>(gameViewDTO(gamePlayerRepository.findById(id).get()), HttpStatus.OK);
    }


//Map -> Recibe una entrada y devuelve una salida
//FlatMap -> Recibe una entrada y devuelve varias salidas

    public List<String> getShipsLocations(GamePlayer gamePlayer) {
        List<String> shipsLocations = gamePlayer.getShips().stream().map(ship -> ship.getLocations()).flatMap(locations -> locations.stream()).collect(Collectors.toList());
        return shipsLocations;
    }

    public List<String> getSalvoesLocations(GamePlayer gamePlayer){
        List<String> salvoesLocations = gamePlayer.getSalvoes().stream().map(salvo -> salvo.getLocations()).flatMap(locations -> locations.stream()).collect(Collectors.toList());
        return salvoesLocations;
    }

    //  Solo traigo las celdas que coinciden en ambas listas, traidas con los dos metodos anteriores
    public List<String> getHitsLocations (GamePlayer gamePlayer){

        List<String> locationsShipsSelf = getShipsLocations(gamePlayer);
        List<String> locationsSalvosOpponent = getSalvoesLocations(getOpponent(gamePlayer));
        return locationsShipsSelf.stream().filter(cell -> locationsSalvosOpponent.contains(cell)).collect(Collectors.toList());
    }

    public Long getCountMissed (GamePlayer gamePlayer){
        List<String> locationsShipsSelf = getShipsLocations(gamePlayer);
        List<String> locationsSalvosOpponent = getSalvoesLocations(getOpponent(gamePlayer));

        Long hits = locationsShipsSelf.stream().filter(cell -> locationsSalvosOpponent.contains(cell)).count();
        Long salvos = locationsSalvosOpponent.stream().count();
        Long missed = salvos - hits;
        return missed;
    }

    //Un game tiene 2 players, busco el oponente de mi gamePlayer, descartando a mi gamePlayer
    public GamePlayer getOpponent (GamePlayer gpSelf){
        return gpSelf.getGame().getGamePlayers().stream().filter(gp -> gp.getId() != gpSelf.getId()).findAny().orElse(null);
    }

    public Map<String, Object> hitsDTO(GamePlayer gamePlayer){
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("self", gpHitsDTO(gamePlayer));
        dto.put("opponent", gpHitsDTO(getOpponent(gamePlayer)));
        return dto;
    }

    public Map<String, Object> gpHitsDTO(GamePlayer gamePlayer) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("turn", 1); //hardcodeado
        dto.put("hitLocations", getHitsLocations(gamePlayer));
        dto.put("damages", getDamages(gamePlayer));
        dto.put("missed", getCountMissed(gamePlayer));
        return dto;
    }

    public Map<String, Object> getDamages(GamePlayer gamePlayer){
        Map<String, Object> dto = new LinkedHashMap<String, Object>();

        dto.put("carrierHits", 0);
        dto.put("battleshipHits", 0);
        dto.put("submarineHits", 0);
        dto.put("destroyerHits", 0);
        dto.put("patrolboatHits", 0);

        dto.put("carrier", 0);
        dto.put("battleship", 0);
        dto.put("submarine", 0);
        dto.put("destroyer", 0);
        dto.put("patrolboat", 0);

        return dto;
    }
//--------------------------------------------------

    private Map<String, Object> gameViewDTO(GamePlayer gamePlayer) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", gamePlayer.getGame().getId());
        dto.put("created", gamePlayer.getGame().getCreated());
        dto.put("gamePlayers", gamePlayer.getGame().getGamePlayers().stream().map(gp -> gamePlayerDTO(gp)));
        dto.put("ships", gamePlayer.getShips().stream().map(ship -> shipDTO(ship)));
        dto.put("salvoes", gamePlayer.getGame().getGamePlayers().stream().flatMap(gamePlayer1 -> gamePlayer1.getSalvoes().stream().map(salvo -> salvoDTO(salvo))));
        dto.put("scores", gamePlayer.getPlayer().getScores().stream().map(score -> scoreDTO(score)));
        dto.put("hits", hitsDTO(gamePlayer));
        return dto;
    }

    private Map<String, Object> playerDTO(Player player) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", player.getId());
        dto.put("username", player.getUsername());
        dto.put("score", getScoreList(player));
        return dto;
    }

    private Map<String, Object> loggedPlayerDTO(Player player) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", player.getId());
        dto.put("name", player.getUsername());
        return dto;
    }

    @RequestMapping("/leaderBoard")
    public List<Object> getAllScores() {
        return playerRepository
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

    //-- Para verificar si el jugador logueado es el que pertenece a tal juego
    private boolean wrongGamePlayer(GamePlayer gamePlayer, Player player) {
        boolean correctGP = gamePlayer.getPlayer().getId() != player.getId();
        return correctGP;
    }

    //----------------------  CREATE NEW PLAYER (SIGN UP)  ----------------------//
    @RequestMapping(path = "/players", method = RequestMethod.POST)
    public ResponseEntity<Object> register(@RequestParam String username, @RequestParam String password) {

        if (username.isEmpty() || password.isEmpty())
            return new ResponseEntity<>("Missing data", HttpStatus.FORBIDDEN);

        if (playerRepository.findByUsername(username) != null)
            return new ResponseEntity<>("El username (" + username + ") se encuentra ocupado.", HttpStatus.FORBIDDEN);

        playerRepository.save(new Player(username, passwordEncoder.encode(password)));

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    //----------------------  CREATE NEW GAME  ----------------------//
    //crea nuevo game con gamePlayer, se le asigna al usuario logueado --> (variables de sesion)
    @RequestMapping(path = "/games", method = RequestMethod.POST)
    public ResponseEntity<Object> newGame(Authentication authentication) {

        Player authenticatedPlayer = getAuthentication(authentication);

        if (authenticatedPlayer == null)
            return new ResponseEntity<>("No name given", HttpStatus.FORBIDDEN);

        else {
            Date date = Date.from(java.time.ZonedDateTime.now().toInstant());

            Game newGame = new Game(date);
            gameRepository.save(newGame);

            GamePlayer newGamePlayer = new GamePlayer(newGame, authenticatedPlayer);
            gamePlayerRepository.save(newGamePlayer);

            return new ResponseEntity<>("Game Created", HttpStatus.CREATED);
        }
    }

    //----------------------  JOIN GAME  ----------------------//
    @RequestMapping(path = "game/{id}/players", method = RequestMethod.POST)
    public ResponseEntity<Object> joinGame(Authentication authentication, @PathVariable long id) {

        Player authenticatedPlayer = getAuthentication(authentication);
        GamePlayer gamePlayer = gamePlayerRepository.findById(id).orElse(null);

        Game gameActual = gameRepository.findById(id).get();

        if (authenticatedPlayer == null)
            return new ResponseEntity<>("No such player", HttpStatus.UNAUTHORIZED);

        if (gameActual == null)
            return new ResponseEntity<>("No such game", HttpStatus.FORBIDDEN);

        if (gameActual.getGamePlayers().size() >= 2)
            return new ResponseEntity<>("Game is full", HttpStatus.FORBIDDEN);

        else {
            GamePlayer newGamePlayer = new GamePlayer(gameActual, authenticatedPlayer);
            gamePlayerRepository.save(newGamePlayer);
            return new ResponseEntity<>("Game join successfully", HttpStatus.CREATED);
        }
    }

    //----------------------  ADD SHIPS  ----------------------//
    @RequestMapping(path = "games/players/{id}/ships", method = RequestMethod.POST)
    public ResponseEntity<Object> addShips(Authentication authentication, @RequestBody List<Ship> ships, @PathVariable long id) {

        Player authenticatedPlayer = getAuthentication(authentication);
        GamePlayer gamePlayer = gamePlayerRepository.findById(id).orElse(null);

        if (authenticatedPlayer == null)
            return new ResponseEntity<>("No player logged in", HttpStatus.UNAUTHORIZED);

        if (gamePlayer == null)
            return new ResponseEntity<>("No such gamePlayer", HttpStatus.UNAUTHORIZED);

        if (wrongGamePlayer(gamePlayer, authenticatedPlayer))
            return new ResponseEntity<>("Wrong gamePlayer", HttpStatus.UNAUTHORIZED);

        else {
            if (gamePlayer.getShips().isEmpty()) {
                ships.forEach(ship -> ship.setGamePlayer(gamePlayer));
                gamePlayer.setShips(ships);
                shipRepository.saveAll(ships);
                return new ResponseEntity<>("Ships saved", HttpStatus.CREATED);
            } else
                return new ResponseEntity<>("Player already has ships", HttpStatus.FORBIDDEN);
        }
    }

    //----------------------  ADD SALVOS  ----------------------//
    @RequestMapping(path = "games/players/{id}/salvoes", method = RequestMethod.POST)
    public ResponseEntity<Object> addSalvoes(Authentication authentication, @RequestBody Salvo salvo, @PathVariable long id) {

        Player authenticatedPlayer = getAuthentication(authentication);
        GamePlayer gamePlayer = gamePlayerRepository.findById(id).orElse(null);

        if (authenticatedPlayer == null)
            return new ResponseEntity<>("No player logged in", HttpStatus.UNAUTHORIZED);
        if (gamePlayer == null)
            return new ResponseEntity<>("No such gamePlayer", HttpStatus.FORBIDDEN);
        if (wrongGamePlayer(gamePlayer, authenticatedPlayer))
            return new ResponseEntity<>("Wrong gamePlayer", HttpStatus.UNAUTHORIZED);

        else {
            if (!hasTurnedSalvo(salvo, gamePlayer.getSalvoes())) {
                gamePlayer.addSalvo(salvo);
                salvo.setGamePlayer(gamePlayer);
                salvoRepository.save(salvo);
                return new ResponseEntity<>("Salvo saved", HttpStatus.CREATED);
            } else
                return new ResponseEntity<>("Player already has salvoes", HttpStatus.FORBIDDEN);
        }
    }

    private boolean hasTurnedSalvo(Salvo newSalvo, List<Salvo> salvosGameplayer) {

        boolean hasSalvoes = false;
        for (Salvo salvo : salvosGameplayer) {
            if (salvo.getTurn() == newSalvo.getTurn())
                hasSalvoes = true;
        }
        return hasSalvoes;
    }





}