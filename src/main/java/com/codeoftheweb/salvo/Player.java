package com.codeoftheweb.salvo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.*;

import static java.util.stream.Collectors.toList;


@Entity
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;
    private String email;

    @OneToMany(mappedBy="player", fetch=FetchType.EAGER)
    private //el mappedBy coincide con atributo Player player de GamePlayer
    Set<GamePlayer> gamePlayers;

    @OneToMany(mappedBy = "score", fetch = FetchType.EAGER)
    private Set<Score> scores;

    public Player() { }

    public Player(String mail) {
        this.email = mail;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<GamePlayer> getGamePlayers() {
        return gamePlayers;
    }

    public void setGamePlayers(Set<GamePlayer> gamePlayers) {
        this.gamePlayers = gamePlayers;
    }

    public void addGamePlayer(GamePlayer gamePlayer) {
        gamePlayer.setPlayer(this);
        getGamePlayers().add(gamePlayer);
    }

    public void addScore(Score score) {
        score.setPlayer(this);
        getScores().add(score);
    }

    @JsonIgnore
    public List<Game> getGames() {
        return getGamePlayers().stream().map(sub -> sub.getGame()).collect(toList());
    }

    @JsonIgnore
    public Set<Score> getScores() {
        return scores;
    }

    public void setScores(Set<Score> scores) {
        this.scores = scores;
    }


    public float getScoreTotal(Player player){
        return player.getWins(player.getScores())*1 + player.getLost(player.getScores())*0 + player.getTied(player.getScores())*(float)0.5;
    }

    public float getWins(Set<Score> scores){
        return scores.stream().filter(score -> score.getScore() == 1).count();
    }

    public float getLost(Set<Score> scores){
        return scores.stream().filter(score -> score.getScore() == 0).count();
    }

    public float getTied(Set<Score> scores){
        return scores.stream().filter(score -> score.getScore() == (float)0.5).count();
    }
}
