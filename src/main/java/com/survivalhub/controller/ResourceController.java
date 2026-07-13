package com.survivalhub.controller;

import com.survivalhub.model.AppUser;
import com.survivalhub.model.TaskResource;
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

import java.util.List;

@RestController
@RequestMapping("/api/worlds/{worldId}/tasks/{taskId}/resources")
public class ResourceController {

    private final ResourceService resourceService;
    private final TaskService taskService;
    private final WorldService worldService;

    public ResourceController(ResourceService resourceService, TaskService taskService, WorldService worldService) {
        this.resourceService = resourceService;
        this.taskService = taskService;
        this.worldService = worldService;
    }

    @GetMapping
    public ResponseEntity<List<TaskResource>> getResourcesByTaskId(
            @PathVariable Long worldId,
            @PathVariable Long taskId,
            Authentication authentication
    ) {
        if (!canAccessWorld(worldId, authentication)) {
            return ResponseEntity.notFound().build();
        }

        if (taskService.getTaskById(worldId, taskId).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<TaskResource> resources = resourceService.getResourcesByTaskId(taskId);

        return ResponseEntity.ok(resources);
    }

    @GetMapping("/{resourceId}")
    public ResponseEntity<TaskResource> getResourceById(
            @PathVariable Long worldId,
            @PathVariable Long taskId,
            @PathVariable Long resourceId,
            Authentication authentication
    ) {
        if (!canAccessWorld(worldId, authentication)) {
            return ResponseEntity.notFound().build();
        }

        if (taskService.getTaskById(worldId, taskId).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return resourceService.getResourceById(taskId, resourceId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<TaskResource> createResource(
            @PathVariable Long worldId,
            @PathVariable Long taskId,
            @RequestBody TaskResource resource,
            Authentication authentication
    ) {
        if (!canAccessWorld(worldId, authentication)) {
            return ResponseEntity.notFound().build();
        }

        if (taskService.getTaskById(worldId, taskId).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        TaskResource createdResource = resourceService.createResource(taskId, resource);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdResource);
    }

    @PutMapping("/{resourceId}")
    public ResponseEntity<TaskResource> updateResource(
            @PathVariable Long worldId,
            @PathVariable Long taskId,
            @PathVariable Long resourceId,
            @RequestBody TaskResource resource,
            Authentication authentication
    ) {
        if (!canAccessWorld(worldId, authentication)) {
            return ResponseEntity.notFound().build();
        }

        if (taskService.getTaskById(worldId, taskId).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return resourceService.updateResource(taskId, resourceId, resource)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{resourceId}")
    public ResponseEntity<Void> deleteResource(
            @PathVariable Long worldId,
            @PathVariable Long taskId,
            @PathVariable Long resourceId,
            Authentication authentication
    ) {
        if (!canAccessWorld(worldId, authentication)) {
            return ResponseEntity.notFound().build();
        }

        if (taskService.getTaskById(worldId, taskId).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        boolean deleted = resourceService.deleteResource(taskId, resourceId);

        if (!deleted) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }

    private boolean canAccessWorld(Long worldId, Authentication authentication) {
        AppUser user = (AppUser) authentication.getPrincipal();

        return worldService.getWorldByIdForOwner(worldId, user.getId()).isPresent();
    }
}
