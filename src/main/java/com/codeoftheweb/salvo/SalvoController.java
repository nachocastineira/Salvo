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

    //---------------------------------------- AUTHENTICATION -------------------------------------------------
    private Player getAuthentication(Authentication authentication) {
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken)
            return null;
        else
            return (playerRepository.findByUsername(authentication.getName()));
    }

    //---------------------------------------- GET_GAMES & GAME_DTO -------------------------------------------------
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

    //---------------------------------------- GET_GAMEPLAYER & GAMEPLAYER_DTO -------------------------------------------------
    public List<Object> getGamePlayersLista(List<GamePlayer> gamePlayers) {
        return gamePlayers
                .stream()
                .map(gamePlayer -> gamePlayerDTO(gamePlayer)) //itero, para ir obteniendo todos los gamePlayer con el metodo de abajo
                .collect(Collectors.toList());
    }

    public Map<String, Object> gamePlayerDTO(GamePlayer gamePlayer) { //el map recibe un gamePlayer, y devuelvo sus datos
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("gpid", gamePlayer.getId());
        dto.put("player", gamePlayer.getPlayer());
        return dto;
    }

    //---------------------------------------- SHIP_DTO & SHIP_LIST -------------------------------------------------
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

    //---------------------------------------- SALVO_DTO & SALVO_LIST -------------------------------------------------
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

    //---------------------------------------- SCORE_DTO & SCORE_LIST -------------------------------------------------
    public Map<String, Object> scoreDTO(Score score) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("playerID", score.getPlayer().getId());
        dto.put("score", score.getScore());
        dto.put("finishDate", score.getFinishDate());
        return dto;
    }

    public List<Object> getScoreLista(List<Score> scores) {
        return scores.stream().map(score -> scoreDTO(score)).collect(Collectors.toList());
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

    //---------------------------------------- GET GAME VIEW -------------------------------------------------
    @RequestMapping("/game_view/{id}")
    public ResponseEntity<Map<String, Object>> getGameView(@PathVariable long id, Authentication authentication) {

        Player authenticatedPlayer = getAuthentication(authentication);
        GamePlayer gamePlayer = gamePlayerRepository.findById(id).orElse(null);

        if (authenticatedPlayer == null)
            return new ResponseEntity<>(makeMap("error","no player logged in"), HttpStatus.FORBIDDEN);

        if (wrongGamePlayer(gamePlayer, authenticatedPlayer))
            return new ResponseEntity<>(makeMap("error","Unauthorized"), HttpStatus.UNAUTHORIZED);

        else
            return new ResponseEntity<>(gameViewDTO(gamePlayerRepository.findById(id).get()), HttpStatus.OK);
    }


    public List<String> getShipsLocations(GamePlayer gamePlayer) {
        List<String> shipsLocations = gamePlayer.getShips().stream().map(ship -> ship.getLocations()).flatMap(locations -> locations.stream()).collect(Collectors.toList());
        return shipsLocations;
    }

    public List<String> getSalvoesLocations(GamePlayer gamePlayer) {
        List<String> salvoesLocations = gamePlayer.getSalvoes().stream().map(salvo -> salvo.getLocations()).flatMap(locations -> locations.stream()).collect(Collectors.toList());
        return salvoesLocations;
    }


    //---------------------------------------- GET OPPONENT -------------------------------------------------
    public GamePlayer getOpponent(GamePlayer gpSelf) {
        return gpSelf.getGame().getGamePlayers().stream().filter(gp -> gp.getId() != gpSelf.getId()).findAny().orElse(null);
    }

    //---------------------------------------- HITS DTO -------------------------------------------------
    public Map<String, Object> hitsDTO(GamePlayer gamePlayer) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("self", getHits(gamePlayer));
        dto.put("opponent", getHits(getOpponent(gamePlayer)));
        return dto;
    }

    /**
     * @param gamePlayer
     * @return dto con hits obtenidos, junto al turno, tiros fallidos, locaciones del hit y daños por turno
     */
    //---------------------------------------- GET HITS -------------------------------------------------
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

    /**
     * @param self
     * @return retorno el estado del juego, segun cada condicional y momento de la partida
     */
    //---------------------------------------- GET GAME STATE -------------------------------------------------
    private String getGameState(GamePlayer self) {

        GamePlayer opponent = getOpponent(self);
        String placeShips = "POSICIONAR";
        String waitingForOpponent = "ESPERANDO_OPONENTE";
        String wait = "ESPERE";
        String play = "JUGAR";
        String won = "GANASTE";
        String lost = "PERDISTE";
        String tie = "EMPATASTE";


        if (self.getShips().size() == 0)
            return placeShips;

        if (opponent == null)
            return waitingForOpponent;

/*        if (opponent.getShips().isEmpty())
            return wait;*/

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

    /**
     * @param selfShips
     * @param opponentSalvoes
     * @return devuelvo true si todos los ships fueron hundidos
     */
    private Boolean allPlayerShipsSunk(List<Ship> selfShips, List<Salvo> opponentSalvoes) {
        Map<String, Object> damages = getDamages(selfShips, opponentSalvoes);

        long selfSunkShips = selfShips
                .stream()
                .filter(ship -> Long.parseLong(String.valueOf(damages.get(ship.getType()))) == ship.getLocations().size())
                .count();
        return selfSunkShips == 5;
    }

    /**
     * @param selfShips
     * @param opponentSalvoes
     * @return dto con mis daños obtenidos
     */
    //---------------------------------------- GET DAMAGES -------------------------------------------------
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


    //---------------------------------------- EXISTS SCORE -------------------------------------------------
    //Para verificar si a un juego X ya se le asoció un score final
    private Boolean existsScore(Score score, Game game) {

        List<Score> scores = game.getScores();
        for (Score s : scores) {
            if (score.getPlayer().getUsername().equals(s.getPlayer().getUsername()))
                return true;
        }
        return false;
    }

    //---------------------------------------- CURRENT TURN -------------------------------------------------
    /**
     * @param self
     * @param opponent
     * @return
     */
    private long getCurrentTurn(GamePlayer self, GamePlayer opponent) {

        int selfGPSalvoes = self.getSalvoes().size();
        int opponentGPSalvoes = opponent.getSalvoes().size();

        int totalSalvoes = selfGPSalvoes + opponentGPSalvoes;

        if (totalSalvoes % 2 == 0)
            return totalSalvoes / 2 + 1;

        return (int) (totalSalvoes / 2.0 + 0.5);
    }

    //---------------------------------------- GAME VIEW DTO -------------------------------------------------
    private Map<String, Object> gameViewDTO(GamePlayer gamePlayer) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", gamePlayer.getGame().getId());
        dto.put("created", gamePlayer.getGame().getCreated());
        dto.put("gameState", getGameState(gamePlayer));
        dto.put("gamePlayers", gamePlayer.getGame().getGamePlayers().stream().map(gp -> gamePlayerDTO(gp)));

        if (getOpponent(gamePlayer) == null ||gamePlayer.getShips().isEmpty())
            dto.put("ships", new ArrayList<>());
        else
            dto.put("ships", gamePlayer.getShips().stream().map(ship -> shipDTO(ship)));

        if (getOpponent(gamePlayer) == null || gamePlayer.getSalvoes().isEmpty())
            dto.put("salvoes", new ArrayList<>());
        else
            dto.put("salvoes", gamePlayer.getGame().getGamePlayers().stream().flatMap(gamePlayer1 -> gamePlayer1.getSalvoes().stream().map(salvo -> salvoDTO(salvo))));

        dto.put("scores", gamePlayer.getPlayer().getScores().stream().map(score -> scoreDTO(score)));

        if (getOpponent(gamePlayer) == null)
            dto.put("hits", emptyHits());
        else {
            if (getOpponent(gamePlayer).getShips().isEmpty() || getOpponent(gamePlayer).getSalvoes().isEmpty())
                dto.put("hits", emptyHits());
            else
                dto.put("hits", hitsDTO(gamePlayer));
        }
        return dto;
    }

    //---------------------------------------- EMPTY HITS -------------------------------------------------
    private Map<String,Object> emptyHits(){
        Map<String,Object> dto = new LinkedHashMap<>();
        dto.put("self", new ArrayList<>());
        dto.put("opponent", new ArrayList<>());
        return dto;
    }

    //---------------------------------------- PLAYER DTO -------------------------------------------------
    private Map<String, Object> playerDTO(Player player) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", player.getId());
        dto.put("username", player.getUsername());
        dto.put("score", getScoreList(player));
        return dto;
    }

    //---------------------------------------- LOGGED PLAYER DTO -------------------------------------------------
    private Map<String, Object> loggedPlayerDTO(Player player) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", player.getId());
        dto.put("name", player.getUsername());
        return dto;
    }

    //---------------------------------------- LEADERBOARD -------------------------------------------------
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

    //---------------------------------------- WRONG GAME_PLAYER -------------------------------------------------
    //-- Para verificar si el jugador logueado es el que pertenece a tal juego
    private boolean wrongGamePlayer(GamePlayer gamePlayer, Player player) {
        boolean correctGP = gamePlayer.getPlayer().getId() != player.getId();
        return correctGP;
    }

    //---------------------------------------- CREATE NEW PLAYER (SIGN UP) -------------------------------------------------
    @RequestMapping(path = "/players", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> register(@RequestParam String username, @RequestParam String password) {

        if (username.isEmpty() || password.isEmpty())
            return new ResponseEntity<>(makeMap("error", "Missing data"), HttpStatus.FORBIDDEN);

        if (username.length()>25)
            return new ResponseEntity<>(makeMap("errorCaract", "Maximo de caracteres excedido"), HttpStatus.FORBIDDEN);

        if (password.length()>25)
            return new ResponseEntity<>(makeMap("errorc", "Maximo de caracteres excedido"), HttpStatus.FORBIDDEN);

        if (playerRepository.findByUsername(username) != null)
            return new ResponseEntity<>(makeMap("error","El username (" + username + ") se encuentra ocupado."), HttpStatus.FORBIDDEN);

        playerRepository.save(new Player(username, passwordEncoder.encode(password)));

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    //---------------------------------------- CREATE NEW GAME -------------------------------------------------
    @RequestMapping(path = "/games", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> newGame(Authentication authentication) {

        Player authenticatedPlayer = getAuthentication(authentication);

        if (authenticatedPlayer == null)
            return new ResponseEntity<>(makeMap("error","No player logged in"), HttpStatus.FORBIDDEN);

            Date date = Date.from(java.time.ZonedDateTime.now().toInstant());
            Game newGame = new Game(date);

            GamePlayer newGamePlayer = new GamePlayer(newGame, authenticatedPlayer);
            authenticatedPlayer.addGamePlayer(newGamePlayer);

            gameRepository.save(newGame);
            gamePlayerRepository.save(newGamePlayer);

        return new ResponseEntity<>(makeMap("gpid", newGamePlayer.getId()), HttpStatus.CREATED);

    }


    //---------------------------------------- JOIN GAME -------------------------------------------------
    /**
     * @param authentication
     * @param id
     * @return
     */
    @RequestMapping(path = "game/{id}/players", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> joinGame(Authentication authentication, @PathVariable long id) {
        Player authenticatedPlayer = getAuthentication(authentication);

        Game gameActual = gameRepository.findById(id).orElse(null);

        if (authenticatedPlayer == null)
            return new ResponseEntity<>(makeMap("error", "No such player"), HttpStatus.UNAUTHORIZED);

        if (gameActual == null)
            return new ResponseEntity<>(makeMap("error", "No such game"), HttpStatus.CONFLICT);

        if (gameActual.getGamePlayers().size() >= 2)
            return new ResponseEntity<>(makeMap("error", "Game is full"), HttpStatus.FORBIDDEN);

        GamePlayer newGamePlayer = new GamePlayer(gameActual, authenticatedPlayer);
        gamePlayerRepository.save(newGamePlayer);
        gameActual.addGamePlayer(newGamePlayer);
        return new ResponseEntity<>(makeMap("gpid", newGamePlayer.getId()), HttpStatus.CREATED);
    }


    //---------------------------------------- ADD SHIPS -------------------------------------------------
    /**
     * @param authentication
     * @param ships
     * @param id
     * @return
     */
    @RequestMapping(path = "games/players/{id}/ships", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> addShips(Authentication authentication, @RequestBody List<Ship> ships, @PathVariable long id) {

        Player authenticatedPlayer = getAuthentication(authentication);
        GamePlayer gamePlayer = gamePlayerRepository.findById(id).orElse(null);

        if (authenticatedPlayer == null)
            return new ResponseEntity<>(makeMap("error", "No player logged in"), HttpStatus.UNAUTHORIZED);

        if (gamePlayer == null)
            return new ResponseEntity<>(makeMap("error","No such gamePlayer"), HttpStatus.UNAUTHORIZED);

        if (wrongGamePlayer(gamePlayer, authenticatedPlayer))
            return new ResponseEntity<>(makeMap("error", "Wrong gamePlayer"), HttpStatus.UNAUTHORIZED);

        else {
            if (gamePlayer.getShips().isEmpty()) {
                ships.forEach(ship -> ship.setGamePlayer(gamePlayer));
                gamePlayer.setShips(ships);
                shipRepository.saveAll(ships);
                return new ResponseEntity<>(makeMap("OK", "¡Barcos posicionados! Prepárate para disparar."), HttpStatus.CREATED);
            } else
                return new ResponseEntity<>(makeMap("error", "Player already has ships"), HttpStatus.FORBIDDEN);
        }
    }


    //---------------------------------------- ADD SALVOS -------------------------------------------------
    /**
     * @param authentication
     * @param salvo
     * @param id
     * @return
     */
    @RequestMapping(path = "games/players/{id}/salvoes", method = RequestMethod.POST)
    public ResponseEntity<Object> addSalvoes(Authentication authentication, @RequestBody Salvo salvo, @PathVariable long id) {

        Player authenticatedPlayer = getAuthentication(authentication);
        GamePlayer gamePlayer = gamePlayerRepository.findById(id).orElse(null);

        if (authenticatedPlayer == null)
            return new ResponseEntity<>(makeMap("error", "No player logged in"), HttpStatus.UNAUTHORIZED);
        if (gamePlayer == null)
            return new ResponseEntity<>(makeMap("error", "No such gamePlayer"), HttpStatus.FORBIDDEN);
        if (wrongGamePlayer(gamePlayer, authenticatedPlayer))
            return new ResponseEntity<>(makeMap("error", "Wrong GamePlayer"), HttpStatus.UNAUTHORIZED);

        else {
            if (!hasTurnedSalvo(salvo, gamePlayer.getSalvoes())) {
                salvo.setTurn(gamePlayer.getSalvoes().size() + 1);
                gamePlayer.addSalvo(salvo);
                salvo.setGamePlayer(gamePlayer);
                salvoRepository.save(salvo);
                return new ResponseEntity<>(makeMap("OK", "¡Disparos lanzados!"), HttpStatus.CREATED);
            } else
                return new ResponseEntity<>(makeMap("error", "Player has fired salvoes in this turn"), HttpStatus.FORBIDDEN);
        }
    }

    //----------------------------------------  -------------------------------------------------
    private boolean hasTurnedSalvo(Salvo newSalvo, List<Salvo> salvosGameplayer) {
        boolean hasSalvoes = false;
        for (Salvo salvo : salvosGameplayer) {
            if (salvo.getTurn() == newSalvo.getTurn())
                hasSalvoes = true;
        }
        return hasSalvoes;
    }

    //---------------------------------------- MAKE MAP (Para retornar mensajes y errores) -------------------------------------------------
    private  Map<String,Object> makeMap(String key, Object value){
        Map<String,Object> map = new LinkedHashMap<>();
        map.put(key,value);
        return map;
    }
}