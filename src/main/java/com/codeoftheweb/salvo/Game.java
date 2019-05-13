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
    private Date creationDate;

    @OneToMany(mappedBy="game", fetch=FetchType.EAGER)
    private //el mappedBy coincide con atributo Game game de GamePlayer
            Set<GamePlayer> gamePlayers;

    @OneToMany(mappedBy = "score", fetch = FetchType.EAGER)
    private Set<Score> scores;

    public Game(){}

    public Game(Date dateGame){
        this.creationDate = dateGame;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public Long getId() {
        return id;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
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

    public Set<GamePlayer> getGamePlayers() {
        return gamePlayers;
    }

    public void setGamePlayers(Set<GamePlayer> gamePlayers) {
        this.gamePlayers = gamePlayers;
    }

    @JsonIgnore
    public Set<Score> getScores() {
        return scores;
    }

    public void setScores(Set<Score> scores) {
        this.scores = scores;
    }
}
