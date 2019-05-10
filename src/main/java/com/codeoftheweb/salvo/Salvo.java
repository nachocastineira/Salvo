package com.codeoftheweb.salvo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

@Entity
public class Salvo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gameplayer_id")
    private GamePlayer gamePlayer;

    private String turn;

    @ElementCollection
    @Column(name = "SalvoLocations")
    private List<String> salvcLocations = new ArrayList<>();

    public Salvo(){}

    public Salvo(GamePlayer gamePlayer, String turn, List<String> salvcLocations) {
        this.gamePlayer = gamePlayer;
        this.turn = turn;
        this.salvcLocations = salvcLocations;
    }

    public Long getId() {
        return id;
    }

    @JsonIgnore
    public GamePlayer getGamePlayer() {
        return gamePlayer;
    }

    public String getTurn() {
        return turn;
    }

    public List<String> getSalvcLocations() {
        return salvcLocations;
    }

    public void setGamePlayer(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
    }

    public void setTurn(String turn) {
        this.turn = turn;
    }

    public void setSalvoLocations(List<String> locations) {
        this.salvcLocations = locations;
    }


}
