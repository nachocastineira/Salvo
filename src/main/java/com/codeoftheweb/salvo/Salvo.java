package com.codeoftheweb.salvo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.*;

@Entity
public class Salvo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gamePlayer_id")
    private GamePlayer gamePlayer;

    private Integer turn;

    @ElementCollection
    @Column(name = "locations")
    private List<String> locations = new ArrayList<>();

    public Salvo(){}

    public Salvo(GamePlayer gamePlayer, Integer turn, List<String> locations) {
        this.gamePlayer = gamePlayer;
        this.turn = turn;
        this.locations = locations;
    }

    public Long getId() {
        return id;
    }

    @JsonIgnore
    public GamePlayer getGamePlayer() {
        return gamePlayer;
    }

    public Integer getTurn() {
        return turn;
    }

    public List<String> getLocations() {
        return locations;
    }

    public void setGamePlayer(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
    }

    public void setTurn(Integer turn) {
        this.turn = turn;
    }

    public void setSalvoLocations(List<String> locations) {
        this.locations = locations;
    }


}
