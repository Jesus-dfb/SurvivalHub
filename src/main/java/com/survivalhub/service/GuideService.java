package com.survivalhub.service;

import com.survivalhub.model.AppUser;
import com.survivalhub.model.Guide;
import com.survivalhub.model.GuideFavorite;
import com.survivalhub.model.GuideRating;
import com.survivalhub.model.GuideResource;
import com.survivalhub.model.GuideStep;
import com.survivalhub.repository.GuideFavoriteRepository;
import com.survivalhub.repository.GuideRatingRepository;
import com.survivalhub.repository.GuideRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class GuideService {

    private final GuideRepository guideRepository;
    private final GuideRatingRepository guideRatingRepository;
    private final GuideFavoriteRepository guideFavoriteRepository;

    public GuideService(
            GuideRepository guideRepository,
            GuideRatingRepository guideRatingRepository,
            GuideFavoriteRepository guideFavoriteRepository
    ) {
        this.guideRepository = guideRepository;
        this.guideRatingRepository = guideRatingRepository;
        this.guideFavoriteRepository = guideFavoriteRepository;
    }

    public List<Guide> getAllGuides() {
        return guideRepository.findAll();
    }

    public Optional<Guide> getGuideById(Long id) {
        return guideRepository.findById(id);
    }

    public void markFavoriteState(List<Guide> guides, AppUser user) {
        if (user == null || guides == null || guides.isEmpty()) {
            return;
        }

        List<GuideFavorite> favorites = guideFavoriteRepository.findByUserId(user.getId());
        List<GuideRating> ratings = guideRatingRepository.findByUserNameIgnoreCase(getRatingUserName(user));
        Set<Long> favoriteGuideIds = new HashSet<>();
        Map<Long, Integer> ratingByGuideId = new HashMap<>();

        for (GuideFavorite favorite : favorites) {
            favoriteGuideIds.add(favorite.getGuideId());
        }

        for (GuideRating rating : ratings) {
            ratingByGuideId.put(rating.getGuideId(), rating.getRating());
        }

        for (Guide guide : guides) {
            guide.setFavoriteByCurrentUser(favoriteGuideIds.contains(guide.getId()));
            guide.setCurrentUserRating(ratingByGuideId.getOrDefault(guide.getId(), 0));
        }
    }

    public void markFavoriteState(Guide guide, AppUser user) {
        if (guide == null || user == null) {
            return;
        }

        guide.setFavoriteByCurrentUser(guideFavoriteRepository.existsByGuideIdAndUserId(guide.getId(), user.getId()));
        guide.setCurrentUserRating(guideRatingRepository
                .findByGuideIdAndUserNameIgnoreCase(guide.getId(), getRatingUserName(user))
                .map(GuideRating::getRating)
                .orElse(0));
    }

    public Guide createGuide(Guide guide) {
        guide.setId(null);
        prepareGuideItems(guide);

        return guideRepository.save(guide);
    }

    public Optional<Guide> updateGuide(Long id, Guide updatedGuide) {
        Optional<Guide> guideOptional = getGuideById(id);

        if (guideOptional.isEmpty()) {
            return Optional.empty();
        }

        Guide guide = guideOptional.get();
        prepareGuideItems(updatedGuide);
        guide.setTitle(updatedGuide.getTitle());
        guide.setGame(updatedGuide.getGame());
        guide.setType(updatedGuide.getType());
        guide.setAuthor(updatedGuide.getAuthor());
        guide.setDifficulty(updatedGuide.getDifficulty());
        guide.setDescription(updatedGuide.getDescription());
        guide.setYoutubeUrl(updatedGuide.getYoutubeUrl());
        guide.setSteps(updatedGuide.getSteps());
        guide.setResources(updatedGuide.getResources());

        return Optional.of(guideRepository.save(guide));
    }

    public Optional<Guide> rateGuide(Long id, String userName, int rating) {
        Optional<Guide> guideOptional = getGuideById(id);

        if (guideOptional.isEmpty() || rating < 1 || rating > 5 || userName == null || userName.isBlank()) {
            return Optional.empty();
        }

        Guide guide = guideOptional.get();
        String normalizedUserName = userName.trim();
        GuideRating guideRating = guideRatingRepository
                .findByGuideIdAndUserNameIgnoreCase(id, normalizedUserName)
                .orElse(new GuideRating(null, id, normalizedUserName, rating, LocalDateTime.now()));

        guideRating.setUserName(normalizedUserName);
        guideRating.setRating(rating);

        if (guideRating.getCreatedAt() == null) {
            guideRating.setCreatedAt(LocalDateTime.now());
        }

        guideRatingRepository.save(guideRating);
        recalculateGuideRating(guide);

        return Optional.of(guideRepository.save(guide));
    }

    public Optional<Guide> incrementImportCount(Long id) {
        Optional<Guide> guideOptional = getGuideById(id);

        if (guideOptional.isEmpty()) {
            return Optional.empty();
        }

        Guide guide = guideOptional.get();
        guide.setImportCount(guide.getImportCount() + 1);

        return Optional.of(guideRepository.save(guide));
    }

    public Optional<Guide> favoriteGuide(Long id, AppUser user) {
        Optional<Guide> guideOptional = getGuideById(id);

        if (guideOptional.isEmpty() || user == null) {
            return Optional.empty();
        }

        if (guideFavoriteRepository.findByGuideIdAndUserId(id, user.getId()).isEmpty()) {
            guideFavoriteRepository.save(new GuideFavorite(null, id, user.getId(), LocalDateTime.now()));
        }

        Guide guide = guideOptional.get();
        guide.setFavoriteByCurrentUser(true);

        return Optional.of(guide);
    }

    public Optional<Guide> unfavoriteGuide(Long id, AppUser user) {
        Optional<Guide> guideOptional = getGuideById(id);

        if (guideOptional.isEmpty() || user == null) {
            return Optional.empty();
        }

        guideFavoriteRepository.deleteByGuideIdAndUserId(id, user.getId());

        Guide guide = guideOptional.get();
        guide.setFavoriteByCurrentUser(false);

        return Optional.of(guide);
    }

    public boolean deleteGuide(Long id) {
        if (!guideRepository.existsById(id)) {
            return false;
        }

        guideRepository.deleteById(id);

        return true;
    }

    public boolean canManageGuide(Guide guide, AppUser user) {
        if (guide == null || user == null) {
            return false;
        }

        if (guide.getAuthorUserId() != null) {
            return guide.getAuthorUserId().equals(user.getId());
        }

        String author = normalize(guide.getAuthor());
        String displayName = normalize(user.getDisplayName());
        String username = normalize(user.getUsername());

        return !author.isBlank() && (author.equals(displayName) || author.equals(username));
    }

    private void prepareGuideItems(Guide guide) {
        int stepNumber = 1;

        if (guide.getAuthor() == null || guide.getAuthor().isBlank()) {
            guide.setAuthor("Comunidad");
        }

        if (guide.getDifficulty() == null || guide.getDifficulty().isBlank()) {
            guide.setDifficulty("Media");
        }

        if (guide.getCreatedAt() == null) {
            guide.setCreatedAt(LocalDateTime.now());
        }

        if (guide.getRatingAverage() < 0) {
            guide.setRatingAverage(0);
        }

        if (guide.getRatingCount() < 0) {
            guide.setRatingCount(0);
        }

        if (guide.getImportCount() < 0) {
            guide.setImportCount(0);
        }

        if (guide.getSteps() == null) {
            guide.setSteps(new ArrayList<>());
        }

        for (GuideStep step : guide.getSteps()) {
            if (step.getStepNumber() <= 0) {
                step.setStepNumber(stepNumber);
            }

            stepNumber++;
        }

        if (guide.getResources() == null) {
            guide.setResources(new ArrayList<>());
        }

        // La base de datos genera los ids de los recursos de la guia.
    }

    private void recalculateGuideRating(Guide guide) {
        List<GuideRating> ratings = guideRatingRepository.findByGuideId(guide.getId());

        if (ratings.isEmpty()) {
            guide.setRatingAverage(0);
            guide.setRatingCount(0);
            return;
        }

        int ratingTotal = 0;

        for (GuideRating rating : ratings) {
            ratingTotal += rating.getRating();
        }

        double average = ratingTotal * 1.0 / ratings.size();
        average = Math.round(average * 100.0) / 100.0;

        guide.setRatingAverage(average);
        guide.setRatingCount(ratings.size());
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private String getRatingUserName(AppUser user) {
        if (user.getDisplayName() != null && !user.getDisplayName().isBlank()) {
            return user.getDisplayName().trim();
        }

        return user.getUsername().trim();
    }
}
