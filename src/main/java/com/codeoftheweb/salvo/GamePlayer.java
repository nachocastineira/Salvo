package com.codeoftheweb.salvo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.*;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;



@Entity
public class GamePlayer {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="game_id")
    private Game game;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="player_id")
    private Player player;

    @OneToMany(mappedBy = "gamePlayer", fetch=FetchType.EAGER)
    private
    List<Ship> ships;

    @OneToMany(mappedBy = "gamePlayer")
    private
    List<Salvo> salvoes;

    private Date created;

    public  GamePlayer(){}

    public GamePlayer(Game game, Player player) {
        this.game = game;
        this.player = player;
        this.created = new Date();
    }

    public Long getId() {
        return id;
    }

    @JsonIgnore
    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    @JsonIgnore
    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Date getCreated() {
        return created;
    }

    public List<Ship> getShips() {
        return ships;
    }

    public void setShips(List<Ship> ships) {
        this.ships = ships;
    }

    public void addShip(Ship ship){
        ship.setGamePlayer(this);
        getShips().add(ship);
    }

    public void addSalvo(Salvo salvo){
        salvo.setGamePlayer(this);
        getSalvoes().add(salvo);
    }

    public List<Salvo> getSalvoes() {
        return salvoes;
    }

    public void setSalvoes(List<Salvo> salvoes) {
        this.salvoes = salvoes;
    }
}