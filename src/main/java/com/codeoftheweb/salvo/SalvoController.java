package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
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
    @Autowired
    private ScoreRepository scoreRepository;

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

    public List<Object> getScoreLista(List<Score> scores) {
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

    private List<Map<String, Object>> makeShipList(List<Ship> ships) {
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

    private List<Map<String, Object>> getShipsList(Game game) {
        List<Map<String, Object>> myList = new ArrayList<>();
        game.getGamePlayers().forEach(gamePlayer -> myList.addAll(makeShipList(gamePlayer.getShips())));
        return myList;
    }

    @RequestMapping("/game_view/{id}")
    public ResponseEntity<Map<String, Object>> getGameView(@PathVariable long id, Authentication authentication) {

        Player authenticatedPlayer = getAuthentication(authentication);
        GamePlayer gamePlayer = gamePlayerRepository.findById(id).orElse(null);

        if (authenticatedPlayer == null) {
            return new ResponseEntity<>(MakeMap("error","no player logged in"), HttpStatus.FORBIDDEN);
        }

        if (wrongGamePlayer(gamePlayer, authenticatedPlayer))
            return new ResponseEntity<>(MakeMap("error","Unauthorized"), HttpStatus.UNAUTHORIZED);

        if(getOpponent(gamePlayer) == null){
            return new ResponseEntity<>(MakeMap("error", "waiting for next player"),HttpStatus.FORBIDDEN);
        }
        else
            return new ResponseEntity<>(gameViewDTO(gamePlayerRepository.findById(id).get()), HttpStatus.OK);
    }


//Map -> Recibe una entrada y devuelve una salida
//FlatMap -> Recibe una entrada y devuelve varias salidas

    public List<String> getShipsLocations(GamePlayer gamePlayer) {
        List<String> shipsLocations = gamePlayer.getShips().stream().map(ship -> ship.getLocations()).flatMap(locations -> locations.stream()).collect(Collectors.toList());
        return shipsLocations;
    }

    public List<String> getSalvoesLocations(GamePlayer gamePlayer) {
        List<String> salvoesLocations = gamePlayer.getSalvoes().stream().map(salvo -> salvo.getLocations()).flatMap(locations -> locations.stream()).collect(Collectors.toList());
        return salvoesLocations;
    }

    //  Solo traigo las celdas que coinciden en ambas listas, traidas con los dos metodos anteriores
    public List<String> getHitsLocations(GamePlayer gamePlayer) {

        List<String> locationsShipsSelf = getShipsLocations(gamePlayer);
        List<String> locationsSalvosOpponent = getSalvoesLocations(getOpponent(gamePlayer));
        return locationsShipsSelf.stream().filter(cell -> locationsSalvosOpponent.contains(cell)).collect(Collectors.toList());
    }

    public Long getCountMissed(GamePlayer gamePlayer) {
        List<String> locationsShipsSelf = getShipsLocations(gamePlayer);
        List<String> locationsSalvosOpponent = getSalvoesLocations(getOpponent(gamePlayer));

        Long hits = locationsShipsSelf.stream().filter(cell -> locationsSalvosOpponent.contains(cell)).count();
        Long salvos = locationsSalvosOpponent.stream().count();
        Long missed = salvos - hits;
        return missed;
    }

    //Un game tiene 2 players, busco el oponente de mi gamePlayer, descartando a mi gamePlayer
    public GamePlayer getOpponent(GamePlayer gpSelf) {
        return gpSelf.getGame().getGamePlayers().stream().filter(gp -> gp.getId() != gpSelf.getId()).findAny().orElse(null);
    }

    public Map<String, Object> hitsDTO(GamePlayer gamePlayer) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("self", getHits(gamePlayer));
        dto.put("opponent", getHits(getOpponent(gamePlayer)));
        return dto;
    }

    public List<Map> getHits(GamePlayer gamePlayer) {

        List<Map> dto = new ArrayList<>();
        Integer carrierHitsTotal = 0;
        Integer battleshipHitsTotal = 0;
        Integer submarineHitsTotal = 0;
        Integer destroyerHitsTotal = 0;
        Integer patrolboatHitsTotal = 0;
        List<String> carrierLocations = new ArrayList<>();
        List<String> battleshipLocations = new ArrayList<>();
        List<String> submarineLocations = new ArrayList<>();
        List<String> destroyerLocations = new ArrayList<>();
        List<String> patrolboatLocations = new ArrayList<>();

        for (Ship ship : gamePlayer.getShips()) {
            switch (ship.getType()) {
                case "carrier":
                    carrierLocations = ship.getLocations();
                    break;
                case "battleship":
                    battleshipLocations = ship.getLocations();
                    break;
                case "submarine":
                    submarineLocations = ship.getLocations();
                    break;
                case "destroyer":
                    destroyerLocations = ship.getLocations();
                    break;
                case "patrolboat":
                    patrolboatLocations = ship.getLocations();
                    break;
            }
        }

        for (Salvo salvo : getOpponent(gamePlayer).getSalvoes()) {

            Integer carrierHitsInTurn = 0;
            Integer battleshipHitsInTurn = 0;
            Integer submarineHitsInTurn = 0;
            Integer destroyerHitsInTurn = 0;
            Integer patrolboatHitsInTurn = 0;
            Integer missedShots = salvo.getLocations().size();
            Map<String, Object> hitsMapPerTurn = new LinkedHashMap<>();
            Map<String, Object> damagesPerTurn = new LinkedHashMap<>();
            List<String> salvoLocationsList = new ArrayList<>();
            List<String> hitCellsList = new ArrayList<>();
            salvoLocationsList.addAll(salvo.getLocations());

            for (String salvoShot : salvoLocationsList) {
                if (carrierLocations.contains(salvoShot)) {
                    carrierHitsInTurn++;
                    carrierHitsTotal++;
                    hitCellsList.add(salvoShot);
                    missedShots--;
                }
                if (battleshipLocations.contains(salvoShot)) {
                    battleshipHitsInTurn++;
                    battleshipHitsTotal++;
                    hitCellsList.add(salvoShot);
                    missedShots--;
                }
                if (submarineLocations.contains(salvoShot)) {
                    submarineHitsInTurn++;
                    submarineHitsTotal++;
                    hitCellsList.add(salvoShot);
                    missedShots--;
                }
                if (destroyerLocations.contains(salvoShot)) {
                    destroyerHitsInTurn++;
                    destroyerHitsTotal++;
                    hitCellsList.add(salvoShot);
                    missedShots--;
                }
                if (patrolboatLocations.contains(salvoShot)) {
                    patrolboatHitsInTurn++;
                    patrolboatHitsTotal++;
                    hitCellsList.add(salvoShot);
                    missedShots--;
                }
            }
            damagesPerTurn.put("carrierHits", carrierHitsInTurn);
            damagesPerTurn.put("battleshipHits", battleshipHitsInTurn);
            damagesPerTurn.put("submarineHits", submarineHitsInTurn);
            damagesPerTurn.put("destroyerHits", destroyerHitsInTurn);
            damagesPerTurn.put("patrolboatHits", patrolboatHitsInTurn);
            damagesPerTurn.put("carrier", carrierHitsTotal);
            damagesPerTurn.put("battleship", battleshipHitsTotal);
            damagesPerTurn.put("submarine", submarineHitsTotal);
            damagesPerTurn.put("destroyer", destroyerHitsTotal);
            damagesPerTurn.put("patrolboat", patrolboatHitsTotal);
            hitsMapPerTurn.put("turn", salvo.getTurn());
            hitsMapPerTurn.put("hitLocations", hitCellsList);
            hitsMapPerTurn.put("damages", damagesPerTurn);
            hitsMapPerTurn.put("missed", missedShots);
            dto.add(hitsMapPerTurn);
        }
        return dto;
    }

    private String getGameState(GamePlayer self) {

        GamePlayer opponent = getOpponent(self);
        String placeShips = "PLACESHIPS";
        String waitingForOpponent = "WAITINGFOROPP";
        String wait = "WAIT";
        String play = "PLAY";
        String won = "WON";
        String lost = "LOST";
        String tie = "TIE";


        if (self.getShips().size() == 0)
            return placeShips;

        if (opponent.getShips() == null)
            return waitingForOpponent;

        if (opponent.getShips().size() == 0)
            return wait;

        long turn = getCurrentTurn(self, opponent);

        if (self.getSalvoes().size() == opponent.getSalvoes().size()) {

            Date date = new Date();
            date = Date.from(date.toInstant());

            if (allPlayerShipsSunk(self.getShips(), opponent.getSalvoes()) && allPlayerShipsSunk(opponent.getShips(), self.getSalvoes())) {

                Score newScore = new Score(self.getGame(), self.getPlayer(), (float) 0.5, date);
                if (!existsScore(newScore, self.getGame()))
                    scoreRepository.save(newScore);
                return tie;
            }
            if (allPlayerShipsSunk(self.getShips(), opponent.getSalvoes())) {
                Score newScore = new Score(self.getGame(), self.getPlayer(), 0, date);
                if (!existsScore(newScore, self.getGame()))
                    scoreRepository.save(newScore);
                return lost;
            }
            if (allPlayerShipsSunk(opponent.getShips(), self.getSalvoes())) {
                Score newScore = new Score(self.getGame(), self.getPlayer(), 1, date);
                if (!existsScore(newScore, self.getGame()))
                    scoreRepository.save(newScore);
                return won;
            }
            if (self.getSalvoes().size() != turn)
                return play;
        }
        return wait;
    }

    private Boolean allPlayerShipsSunk(List<Ship> selfShips, List<Salvo> opponentSalvoes) {
        Map<String, Object> damages = getDamages(selfShips, opponentSalvoes);

        long selfSunkShips = selfShips
                .stream()
                .filter(ship -> Long.parseLong(String.valueOf(damages.get(ship.getType()))) == ship.getLocations().size())
                .count();
        return selfSunkShips == 5;
    }

    private Map<String, Object> getDamages(List<Ship> selfShips, List<Salvo> opponentSalvoes) {

        Map<String, Object> dto = new LinkedHashMap<>();
        Integer carrierDamaged = 0;
        Integer battleshipDamaged = 0;
        Integer submarineDamaged = 0;
        Integer destroyerDamaged = 0;
        Integer patrolboatDamaged = 0;
        List<String> carrierLocations = new ArrayList<>();
        List<String> battleshipLocations = new ArrayList<>();
        List<String> submarineLocations = new ArrayList<>();
        List<String> destroyerLocations = new ArrayList<>();
        List<String> patrolboatLocations = new ArrayList<>();

        for (Ship ship : selfShips) {
            switch (ship.getType()) {
                case "carrier":
                    carrierLocations = ship.getLocations();
                    break;
                case "battleship":
                    battleshipLocations = ship.getLocations();
                    break;
                case "submarine":
                    submarineLocations = ship.getLocations();
                    break;
                case "destroyer":
                    destroyerLocations = ship.getLocations();
                    break;
                case "patrolboat":
                    patrolboatLocations = ship.getLocations();
                    break;
            }
        }

        for (Salvo salvo : opponentSalvoes) {
            List<String> salvoLocationsList = new ArrayList<>();
            salvoLocationsList.addAll(salvo.getLocations());


            for (String salvoShot : salvoLocationsList) {
                if (carrierLocations.contains(salvoShot))
                    carrierDamaged++;

                if (battleshipLocations.contains(salvoShot))
                    battleshipDamaged++;

                if (submarineLocations.contains(salvoShot))
                    submarineDamaged++;

                if (destroyerLocations.contains(salvoShot))
                    destroyerDamaged++;

                if (patrolboatLocations.contains(salvoShot))
                    patrolboatDamaged++;
            }
        }

        dto.put("carrier", carrierDamaged);
        dto.put("battleship", battleshipDamaged);
        dto.put("submarine", submarineDamaged);
        dto.put("destroyer", destroyerDamaged);
        dto.put("patrolboat", patrolboatDamaged);
        return dto;
    }


    //Para verificar si a un juego X ya se le asoció un score final
    private Boolean existsScore(Score score, Game game) {

        List<Score> scores = game.getScores();
        for (Score s : scores) {
            if (score.getPlayer().getUsername().equals(s.getPlayer().getUsername()))
                return true;
        }
        return false;
    }

    private long getCurrentTurn(GamePlayer self, GamePlayer opponent) {

        int selfGPSalvoes = self.getSalvoes().size();
        int opponentGPSalvoes = opponent.getSalvoes().size();

        int totalSalvoes = selfGPSalvoes + opponentGPSalvoes;

        if (totalSalvoes % 2 == 0)
            return totalSalvoes / 2 + 1;

        return (int) (totalSalvoes / 2.0 + 0.5);
    }


    private Map<String, Object> gameViewDTO(GamePlayer gamePlayer) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", gamePlayer.getGame().getId());
        dto.put("created", gamePlayer.getGame().getCreated());
        dto.put("gameState", getGameState(gamePlayer));
        dto.put("gamePlayers", gamePlayer.getGame().getGamePlayers().stream().map(gp -> gamePlayerDTO(gp)));

        if (gamePlayer.getShips().isEmpty())
            dto.put("ships", new ArrayList<>());
        else
            dto.put("ships", gamePlayer.getShips().stream().map(ship -> shipDTO(ship)));

        if (getOpponent(gamePlayer) == null || gamePlayer.getSalvoes().isEmpty())
            dto.put("salvoes", new ArrayList<>());
        else
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
    public ResponseEntity<Map<String, Object>> newGame(Authentication authentication) {

        Player authenticatedPlayer = getAuthentication(authentication);

        if (authenticatedPlayer == null)
            return new ResponseEntity<>(MakeMap("error","No player logged in"), HttpStatus.FORBIDDEN);


            Date date = Date.from(java.time.ZonedDateTime.now().toInstant());
            Game newGame = new Game(date);

            GamePlayer newGamePlayer = new GamePlayer(newGame, authenticatedPlayer);
            authenticatedPlayer.addGamePlayer(newGamePlayer);

            gameRepository.save(newGame);
            gamePlayerRepository.save(newGamePlayer);

        return new ResponseEntity<>(MakeMap("gpid", newGamePlayer.getId()), HttpStatus.CREATED);

    }

    //----------------------  JOIN GAME  ----------------------//
    @RequestMapping(path = "game/{id}/players", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> joinGame(Authentication authentication, @PathVariable long id) {

        Player authenticatedPlayer = getAuthentication(authentication);

        Game gameActual = gameRepository.findById(id).get();

        if (authenticatedPlayer == null)
            return new ResponseEntity<>(MakeMap("error", "No such player"), HttpStatus.UNAUTHORIZED);

        if (gameActual == null)
            return new ResponseEntity<>(MakeMap("error", "No such game"), HttpStatus.CONFLICT);

        if (gameActual.getGamePlayers().size() >= 2)
            return new ResponseEntity<>(MakeMap("error", "Game is full"), HttpStatus.FORBIDDEN);

        GamePlayer newGamePlayer = new GamePlayer(gameActual, authenticatedPlayer);
        gamePlayerRepository.save(newGamePlayer);
        gameActual.addGamePlayer(newGamePlayer);
        return new ResponseEntity<>(MakeMap("gpid", newGamePlayer.getId()), HttpStatus.CREATED);
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
                salvo.setTurn(gamePlayer.getSalvoes().size() + 1);
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

    //----------------------------------------Creates a map-------------------------------------------------
    private  Map<String,Object> MakeMap(String key, Object value){
        Map<String,Object> map = new LinkedHashMap<>();
        map.put(key,value);
        return map;
    }
}