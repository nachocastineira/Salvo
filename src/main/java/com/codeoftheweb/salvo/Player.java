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
    private String username;

    @OneToMany(mappedBy="player", fetch=FetchType.EAGER)
    private //el mappedBy coincide con atributo Player player de GamePlayer
    Set<GamePlayer> gamePlayers;

    @OneToMany(mappedBy = "player", fetch = FetchType.EAGER)
    private Set<Score> scores;

    private String password;

    public Player() { }

    public Player(String mail, String password) {
        this.username = mail;
        this.password = password;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public double getScoreTotal(Player player){
        return getWins(player.getScores())*1
                + getLost(player.getScores())*0
                + getTied(player.getScores())*(float)0.5;
    }

    public long getWins(Set<Score> scores){
        return scores.stream().filter(score -> score.getScore() == 1).count();
    }

    public long getLost(Set<Score> scores){
        return scores.stream().filter(score -> score.getScore() == 0).count();
    }

    public long getTied(Set<Score> scores){
        return scores.stream().filter(score -> score.getScore() == 0.5).count();
    }
}
