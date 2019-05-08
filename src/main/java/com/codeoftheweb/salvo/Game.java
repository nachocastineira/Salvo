package com.codeoftheweb.salvo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;
import java.util.List;

import static java.util.stream.Collectors.toList;


@Entity
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;
    private Date creationDate;



    //Constructores
    public Game(){}

    public Game(Date dateGame){
        this.creationDate = dateGame;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }



    @OneToMany(mappedBy="game", fetch=FetchType.EAGER) //el mappedBy coincide con atributo Game game de GamePlayer
            Set<GamePlayer> gamePlayers;

    //Getters y Setters

    public Set<GamePlayer> getGamePlayers() {
        return gamePlayers;
    }

    public void setGamePlayers(Set<GamePlayer> gamePlayers) {
        this.gamePlayers = gamePlayers;
    }

    public void addGamePlayer(GamePlayer gamePlayer) {
        gamePlayer.setGame(this);
        gamePlayers.add(gamePlayer);
    }



    @JsonIgnore
    public List<Player> getPlayers() {

        return gamePlayers.stream().map(sub -> sub.getPlayer()).collect(toList());
    }



}
