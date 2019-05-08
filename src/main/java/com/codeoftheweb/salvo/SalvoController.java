package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import  org.springframework.web.bind.annotation.RestController;


import java.util.List;


@RestController
@RequestMapping("/api")
    public class SalvoController {

    @Autowired
    private GameRepository gameRepo;

    @RequestMapping("/games")
    public List<Game> getGames(){
        return gameRepo.findAll();
    }

}

