package com.survivalhub.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "worlds")
public class World {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String game;

    private Long ownerUserId;

    @Column(columnDefinition = "TEXT")
    private String description;

    public World() {
    }

    public World(Long id, String name, String game, String description) {
        this.id = id;
        this.name = name;
        this.game = game;
        this.description = description;
    }

    public World(Long id, String name, String game, Long ownerUserId, String description) {
        this.id = id;
        this.name = name;
        this.game = game;
        this.ownerUserId = ownerUserId;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGame() {
        return game;
    }

    public void setGame(String game) {
        this.game = game;
    }

    public Long getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(Long ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
