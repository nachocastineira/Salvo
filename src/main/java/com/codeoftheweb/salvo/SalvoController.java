package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import  org.springframework.web.bind.annotation.RestController;
import java.util.*;


@RestController
@RequestMapping("/api")
    public class SalvoController {

    @Autowired
    private GameRepository gameRepo;

/*    @RequestMapping("/games")
    public List<Game> getGames(){
        return gameRepo.findAll();
    }*/

    @RequestMapping("/games")
    public  Map<Integer, ArrayList<Game>> getAll(){
        Map<Integer,ArrayList<Game>> games = new HashMap<Integer,ArrayList<Game>>();

        ArrayList<Game> lista = (ArrayList<Game>) gameRepo.findAll();

        games.put(1, lista);

        return games;

    }

}

