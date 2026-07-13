package com.survivalhub.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "guides")
public class Guide {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String game;

    private String type;

    private String author;

    private Long authorUserId;

    private String difficulty;

    private Double ratingAverage;

    private Integer ratingCount;

    private Integer importCount;

    private LocalDateTime createdAt;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String youtubeUrl;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "guide_id")
    @OrderBy("stepNumber ASC")
    private List<GuideStep> steps = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "guide_id")
    private List<GuideResource> resources = new ArrayList<>();

    @Transient
    private boolean favoriteByCurrentUser;

    @Transient
    private Integer currentUserRating;

    public Guide() {
    }

    public Guide(
            Long id,
            String title,
            String game,
            String type,
            String author,
            String difficulty,
            double ratingAverage,
            int ratingCount,
            int importCount,
            LocalDateTime createdAt,
            String description,
            String youtubeUrl,
            List<GuideStep> steps,
            List<GuideResource> resources
    ) {
        this.id = id;
        this.title = title;
        this.game = game;
        this.type = type;
        this.author = author;
        this.authorUserId = null;
        this.difficulty = difficulty;
        this.ratingAverage = ratingAverage;
        this.ratingCount = ratingCount;
        this.importCount = importCount;
        this.createdAt = createdAt;
        this.description = description;
        this.youtubeUrl = youtubeUrl;
        this.steps = steps;
        this.resources = resources;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGame() {
        return game;
    }

    public void setGame(String game) {
        this.game = game;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Long getAuthorUserId() {
        return authorUserId;
    }

    public void setAuthorUserId(Long authorUserId) {
        this.authorUserId = authorUserId;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public double getRatingAverage() {
        return ratingAverage == null ? 0 : ratingAverage;
    }

    public void setRatingAverage(double ratingAverage) {
        this.ratingAverage = ratingAverage;
    }

    public int getRatingCount() {
        return ratingCount == null ? 0 : ratingCount;
    }

    public void setRatingCount(int ratingCount) {
        this.ratingCount = ratingCount;
    }

    public int getImportCount() {
        return importCount == null ? 0 : importCount;
    }

    public void setImportCount(int importCount) {
        this.importCount = importCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getYoutubeUrl() {
        return youtubeUrl;
    }

    public void setYoutubeUrl(String youtubeUrl) {
        this.youtubeUrl = youtubeUrl;
    }

    public List<GuideStep> getSteps() {
        return steps;
    }

    public void setSteps(List<GuideStep> steps) {
        this.steps = steps;
    }

    public List<GuideResource> getResources() {
        return resources;
    }

    public void setResources(List<GuideResource> resources) {
        this.resources = resources;
    }

    public boolean isFavoriteByCurrentUser() {
        return favoriteByCurrentUser;
    }

    public void setFavoriteByCurrentUser(boolean favoriteByCurrentUser) {
        this.favoriteByCurrentUser = favoriteByCurrentUser;
    }

    public int getCurrentUserRating() {
        return currentUserRating == null ? 0 : currentUserRating;
    }

    public void setCurrentUserRating(int currentUserRating) {
        this.currentUserRating = currentUserRating;
    }
}
