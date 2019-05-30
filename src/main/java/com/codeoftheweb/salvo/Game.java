package com.codeoftheweb.salvo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.*;


import static java.util.stream.Collectors.toList;


@Entity
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;
    private Date created;

    @OneToMany(mappedBy="game")
    private //el mappedBy coincide con atributo Game game de GamePlayer
            List<GamePlayer> gamePlayers;

    @OneToMany(mappedBy = "game")
    private List<Score> scores;

    public Game(){}

    public Game(Date dateGame){
        this.created = dateGame;
    }

    public Date getCreated() {
        return created;
    }

    public Long getId() {
        return id;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public void addGamePlayer(GamePlayer gamePlayer) {
        gamePlayer.setGame(this);
        getGamePlayers().add(gamePlayer);
    }

    public void addScore(Score score) {
        score.setGame(this);
        getScores().add(score);
    }

    @JsonIgnore
    public List<Player> getPlayers() {
        return getGamePlayers().stream().map(sub -> sub.getPlayer()).collect(toList());
    }

    public List<GamePlayer> getGamePlayers() {
        return gamePlayers;
    }

    public void setGamePlayers(List<GamePlayer> gamePlayers) {
        this.gamePlayers = gamePlayers;
    }

    @JsonIgnore
    public List<Score> getScores() {
        return scores;
    }

    public void setScores(List<Score> scores) {
        this.scores = scores;
    }
}
