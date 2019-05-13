package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import  org.springframework.web.bind.annotation.RestController;
import java.util.*;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api")
public class SalvoController {

    @Autowired
    private GameRepository gameRepository;

    @RequestMapping("/games")
    public  List<Object> getAll(){
        return gameRepository
                .findAll()
                .stream()
                .map(game -> gameDTO(game)) //devuelvo un solo map con dos registros {1 juego -> 2 jugadores}
                .collect(Collectors.toList());
    }

    public Map<String, Object> gameDTO(Game game) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", game.getId());
        dto.put("creationDate", game.getCreationDate().getTime());
        dto.put("gamePlayers", getGamePlayersLista(game.getGamePlayers())); //obtengo todos los jugadores de ese juego, le mando los gamePlayer de ese juego
        return dto;
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


    private Map<String, Object> playerDTO(Player player){
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", player.getId());
        dto.put("email", player.getEmail());
        dto.put("scores", player.getScores());
        return dto;
    }

    private Map<String,Object> shipDTO(Ship ship){
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("shipType", ship.getType());
        dto.put("shipLocations", ship.getLocations());
        dto.put("player", ship.getGamePlayer().getPlayer().getId());
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
        dto.put("id", salvo.getId());
        dto.put("turn", salvo.getTurn());
        dto.put("player", salvo.getGamePlayer().getPlayer().getId());
        dto.put("locations", salvo.getLocations());
        return dto;
    }


    public Map<String, Object> scoreDTO(Score score){
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", score.getId());
        dto.put("player", score.getPlayer());
        dto.put("game", score.getGame());
        dto.put("score", score.getScore());
        return dto;
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
        dto.put("created", gamePlayer.getGame().getCreationDate());
        dto.put("gamePlayers", getGamePlayersLista(gamePlayer.getGame().getGamePlayers()));
        dto.put("ships", gamePlayer.getShips());
        dto.put("salvoes", gamePlayer.getGame().getGamePlayers().stream()
                .flatMap(gamePlayer1 -> gamePlayer1.getSalvoes().stream().map(salvo -> salvoDTO (salvo))));
        dto.put("scores", gamePlayer.getGame().getScores());

        return dto;
    }

    @Autowired
    private PlayerRepository playerRepository;

    @RequestMapping("/leaderBoard")
    public Map<String, Object> makeLeaderBoard(Player player){
        return playerRepository
                .findAll()
                .stream()
                .map(player -> playerLeaderBoard(player))
                .collect(Collectors.toList());
    }

    private Map<String,Object> playerLeaderBoard(Player player){
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("total", player.getScoreTotal(player));
        dto.put("won", player.getWins(player.getScores()));
        dto.put("lost", player.getLost(player.getScores()));
        dto.put("tied", player.getTied(player.getScores()));
        return dto;
    }
}