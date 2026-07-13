package com.survivalhub.controller;

import com.survivalhub.model.AppUser;
import com.survivalhub.model.Guide;
import com.survivalhub.model.GuideApplicationResult;
import com.survivalhub.model.GuideRatingRequest;
import com.survivalhub.model.GuideResource;
import com.survivalhub.model.GuideStep;
import com.survivalhub.model.Task;
import com.survivalhub.model.TaskResource;
import com.survivalhub.service.GuideService;
import com.survivalhub.service.ResourceService;
import com.survivalhub.service.TaskService;
import com.survivalhub.service.WorldService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/guides")
public class GuideController {

    private final GuideService guideService;
    private final WorldService worldService;
    private final TaskService taskService;
    private final ResourceService resourceService;

    public GuideController(
            GuideService guideService,
            WorldService worldService,
            TaskService taskService,
            ResourceService resourceService
    ) {
        this.guideService = guideService;
        this.worldService = worldService;
        this.taskService = taskService;
        this.resourceService = resourceService;
    }

    @GetMapping
    public List<Guide> getAllGuides(Authentication authentication) {
        List<Guide> guides = guideService.getAllGuides();
        AppUser user = getOptionalAuthenticatedUser(authentication);
        guideService.markFavoriteState(guides, user);

        return guides;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Guide> getGuideById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Optional<Guide> guideOptional = guideService.getGuideById(id);

        if (guideOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Guide guide = guideOptional.get();
        guideService.markFavoriteState(guide, getOptionalAuthenticatedUser(authentication));

        return ResponseEntity.ok(guide);
    }

    @PostMapping
    public ResponseEntity<Guide> createGuide(
            @RequestBody Guide guide,
            Authentication authentication
    ) {
        AppUser user = getAuthenticatedUser(authentication);
        guide.setAuthorUserId(user.getId());
        guide.setAuthor(user.getDisplayName() == null || user.getDisplayName().isBlank()
                ? user.getUsername()
                : user.getDisplayName());

        Guide createdGuide = guideService.createGuide(guide);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdGuide);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Guide> updateGuide(
            @PathVariable Long id,
            @RequestBody Guide guide,
            Authentication authentication
    ) {
        AppUser user = getAuthenticatedUser(authentication);
        Optional<Guide> existingGuide = guideService.getGuideById(id);

        if (existingGuide.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        if (!guideService.canManageGuide(existingGuide.get(), user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        guide.setAuthorUserId(existingGuide.get().getAuthorUserId());
        guide.setAuthor(existingGuide.get().getAuthor());

        return guideService.updateGuide(id, guide)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGuide(
            @PathVariable Long id,
            Authentication authentication
    ) {
        AppUser user = getAuthenticatedUser(authentication);
        Optional<Guide> existingGuide = guideService.getGuideById(id);

        if (existingGuide.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        if (!guideService.canManageGuide(existingGuide.get(), user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        boolean deleted = guideService.deleteGuide(id);

        if (!deleted) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/rating")
    public ResponseEntity<Guide> rateGuide(
            @PathVariable Long id,
            @RequestBody GuideRatingRequest ratingRequest,
            Authentication authentication
    ) {
        AppUser user = getAuthenticatedUser(authentication);
        String userName = user.getDisplayName() == null || user.getDisplayName().isBlank()
                ? user.getUsername()
                : user.getDisplayName();

        return guideService.rateGuide(id, userName, ratingRequest.getRating())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }

    @PostMapping("/{id}/favorite")
    public ResponseEntity<Guide> favoriteGuide(
            @PathVariable Long id,
            Authentication authentication
    ) {
        AppUser user = getAuthenticatedUser(authentication);

        return guideService.favoriteGuide(id, user)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}/favorite")
    public ResponseEntity<Guide> unfavoriteGuide(
            @PathVariable Long id,
            Authentication authentication
    ) {
        AppUser user = getAuthenticatedUser(authentication);

        return guideService.unfavoriteGuide(id, user)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{guideId}/apply/worlds/{worldId}")
    public ResponseEntity<GuideApplicationResult> applyGuideToWorld(
            @PathVariable Long guideId,
            @PathVariable Long worldId,
            Authentication authentication
    ) {
        AppUser user = getAuthenticatedUser(authentication);

        if (worldService.getWorldByIdForOwner(worldId, user.getId()).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Optional<Guide> guideOptional = guideService.getGuideById(guideId);

        if (guideOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Guide guide = guideOptional.get();
        Task task = new Task(
                null,
                worldId,
                guide.getTitle(),
                buildTaskDescription(guide),
                "Media",
                "",
                false
        );
        Task createdTask = taskService.createTask(worldId, task);
        List<TaskResource> createdResources = new ArrayList<>();

        for (GuideResource guideResource : guide.getResources()) {
            TaskResource resource = new TaskResource(
                    null,
                    createdTask.getId(),
                    guideResource.getName(),
                    guideResource.getRequiredQuantity(),
                    0
            );
            createdResources.add(resourceService.createResource(createdTask.getId(), resource));
        }

        Guide updatedGuide = guideService.incrementImportCount(guide.getId()).orElse(guide);
        GuideApplicationResult result = new GuideApplicationResult(updatedGuide, createdTask, createdResources);

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    private String buildTaskDescription(Guide guide) {
        StringBuilder description = new StringBuilder();
        description.append(guide.getDescription() == null ? "" : guide.getDescription());

        if (guide.getAuthor() != null && !guide.getAuthor().isBlank()) {
            description.append("\n\nGuia publicada por: ").append(guide.getAuthor());
        }

        if (guide.getDifficulty() != null && !guide.getDifficulty().isBlank()) {
            description.append("\nDificultad: ").append(guide.getDifficulty());
        }

        if (guide.getYoutubeUrl() != null && !guide.getYoutubeUrl().isBlank()) {
            description.append("\n\nVideo: ").append(guide.getYoutubeUrl());
        }

        if (guide.getSteps() != null && !guide.getSteps().isEmpty()) {
            description.append("\n\nPasos:");

            for (GuideStep step : guide.getSteps()) {
                description.append("\n")
                        .append(step.getStepNumber())
                        .append(". ")
                        .append(step.getTitle());

                if (step.getDescription() != null && !step.getDescription().isBlank()) {
                    description.append(" - ").append(step.getDescription());
                }
            }
        }

        return description.toString();
    }

    private AppUser getAuthenticatedUser(Authentication authentication) {
        return (AppUser) authentication.getPrincipal();
    }

    private AppUser getOptionalAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AppUser user)) {
            return null;
        }

        return user;
    }
}
