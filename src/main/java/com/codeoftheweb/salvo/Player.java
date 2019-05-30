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
    private long id;
    private String username;

    @OneToMany(mappedBy="player", fetch=FetchType.EAGER)
    private //el mappedBy coincide con atributo Player player de GamePlayer
            List<GamePlayer> gamePlayers;

    @OneToMany(mappedBy = "player")
    private List<Score> scores;

    private String password;

    public Player() { }

    public Player(String mail, String password) {
        this.username = mail;
        this.password = password;
    }

    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<GamePlayer> getGamePlayers() {
        return gamePlayers;
    }

    public void setGamePlayers(List<GamePlayer> gamePlayers) {
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
    public List<Score> getScores() {
        return scores;
    }

    public void setScores(List<Score> scores) {
        this.scores = scores;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public float getScoreTotal(Player player){
        return getWins(player.getScores())*1
                + getDraws(player.getScores())*((float)0.5)
                + getLoses(player.getScores())*0;
    }

    public float getWins(List<Score> scores){
        return scores.stream().filter(score -> score.getScore() == 1).count();
    }

    public float getLoses(List<Score> scores){
        return scores.stream().filter(score -> score.getScore() == 0).count();
    }

    public float getDraws(List<Score> scores){
        return scores.stream().filter(score -> score.getScore() == (float)0.5).count();
    }


}
