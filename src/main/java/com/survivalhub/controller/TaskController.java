package com.survivalhub.controller;

import com.survivalhub.model.AppUser;
import com.survivalhub.model.Task;
import com.survivalhub.model.TaskProgressSummary;
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
@RequestMapping("/api/worlds/{worldId}/tasks")
public class TaskController {

    private final TaskService taskService;
    private final WorldService worldService;
    private final ResourceService resourceService;

    public TaskController(TaskService taskService, WorldService worldService, ResourceService resourceService) {
        this.taskService = taskService;
        this.worldService = worldService;
        this.resourceService = resourceService;
    }

    @GetMapping
    public ResponseEntity<List<Task>> getTasksByWorldId(
            @PathVariable Long worldId,
            Authentication authentication
    ) {
        if (!canAccessWorld(worldId, authentication)) {
            return ResponseEntity.notFound().build();
        }

        List<Task> tasks = taskService.getTasksByWorldId(worldId);

        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<Task> getTaskById(
            @PathVariable Long worldId,
            @PathVariable Long taskId,
            Authentication authentication
    ) {
        if (!canAccessWorld(worldId, authentication)) {
            return ResponseEntity.notFound().build();
        }

        return taskService.getTaskById(worldId, taskId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{taskId}/summary")
    public ResponseEntity<TaskProgressSummary> getTaskProgressSummary(
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

        TaskProgressSummary summary = resourceService.getTaskProgressSummary(taskId);

        return ResponseEntity.ok(summary);
    }

    @PostMapping
    public ResponseEntity<Task> createTask(
            @PathVariable Long worldId,
            @RequestBody Task task,
            Authentication authentication
    ) {
        if (!canAccessWorld(worldId, authentication)) {
            return ResponseEntity.notFound().build();
        }

        Task createdTask = taskService.createTask(worldId, task);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<Task> updateTask(
            @PathVariable Long worldId,
            @PathVariable Long taskId,
            @RequestBody Task task,
            Authentication authentication
    ) {
        if (!canAccessWorld(worldId, authentication)) {
            return ResponseEntity.notFound().build();
        }

        return taskService.updateTask(worldId, taskId, task)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/order")
    public ResponseEntity<List<Task>> reorderTasks(
            @PathVariable Long worldId,
            @RequestBody List<Long> taskIds,
            Authentication authentication
    ) {
        if (!canAccessWorld(worldId, authentication)) {
            return ResponseEntity.notFound().build();
        }

        List<Task> tasks = taskService.reorderTasks(worldId, taskIds);

        return ResponseEntity.ok(tasks);
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long worldId,
            @PathVariable Long taskId,
            Authentication authentication
    ) {
        if (!canAccessWorld(worldId, authentication)) {
            return ResponseEntity.notFound().build();
        }

        boolean deleted = taskService.deleteTask(worldId, taskId);

        if (!deleted) {
            return ResponseEntity.notFound().build();
        }

        resourceService.deleteResourcesByTaskId(taskId);

        return ResponseEntity.noContent().build();
    }

    private boolean canAccessWorld(Long worldId, Authentication authentication) {
        AppUser user = (AppUser) authentication.getPrincipal();

        return worldService.getWorldByIdForOwner(worldId, user.getId()).isPresent();
    }
}
