package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import  org.springframework.web.bind.annotation.RestController;
import java.util.*;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api")
    public class SalvoController {

    @Autowired
    private GameRepository gameRepo;

/*    @RequestMapping("/games")
    public List<Game> getGames(){
        return gameRepo.findAll();
    }*/

    // - Metodo para filtrar y mostrar solo los IDs de Games dentro de un array
/*    @RequestMapping("/gamesid")
    public  List<Long> getIdsDeGames(){
        List<Game> games = new ArrayList<>();
        List<Long> i = new ArrayList<>(); //un list para almacenar solo los ids de Game
        games = gameRepo.findAll(); //en la lista almaceno todos los objetos Game

        //Un for acumulador para almacenar los ids en la lista "i" previamente creada
        for (Game game:games){
            i.add(game.getId());
        }

        return i;
    }*/


    @RequestMapping("/games")
    public  List<Object> getGamesInMap(){

        return gameRepo
                .findAll()
                .stream()
                .map(g -> g.dtoGames())
                .collect(Collectors.toList());
    }

}

