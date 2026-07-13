package com.survivalhub.controller;

import com.survivalhub.model.AppUser;
import com.survivalhub.model.World;
import com.survivalhub.model.Task;
import com.survivalhub.service.MemberService;
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
@RequestMapping("/api/worlds")
public class WorldController {

    private final WorldService worldService;
    private final MemberService memberService;
    private final TaskService taskService;
    private final ResourceService resourceService;

    public WorldController(
            WorldService worldService,
            MemberService memberService,
            TaskService taskService,
            ResourceService resourceService
    ) {
        this.worldService = worldService;
        this.memberService = memberService;
        this.taskService = taskService;
        this.resourceService = resourceService;
    }

    @GetMapping
    public List<World> getAllWorlds(Authentication authentication) {
        AppUser user = getAuthenticatedUser(authentication);

        return worldService.getWorldsByOwner(user.getId());
    }

    @GetMapping("/{id}")
    public ResponseEntity<World> getWorldById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        AppUser user = getAuthenticatedUser(authentication);

        return worldService.getWorldByIdForOwner(id, user.getId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<World> createWorld(
            @RequestBody World world,
            Authentication authentication
    ) {
        AppUser user = getAuthenticatedUser(authentication);
        World createdWorld = worldService.createWorldForOwner(world, user.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(createdWorld);
    }

    @PutMapping("/{id}")
    public ResponseEntity<World> updateWorld(
            @PathVariable Long id,
            @RequestBody World world,
            Authentication authentication
    ) {
        AppUser user = getAuthenticatedUser(authentication);

        return worldService.updateWorldForOwner(id, user.getId(), world)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorld(
            @PathVariable Long id,
            Authentication authentication
    ) {
        AppUser user = getAuthenticatedUser(authentication);

        if (worldService.getWorldByIdForOwner(id, user.getId()).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<Task> tasks = taskService.getTasksByWorldId(id);

        for (Task task : tasks) {
            resourceService.deleteResourcesByTaskId(task.getId());
        }

        memberService.deleteMembersByWorldId(id);
        taskService.deleteTasksByWorldId(id);

        boolean deleted = worldService.deleteWorldForOwner(id, user.getId());

        if (!deleted) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }

    private AppUser getAuthenticatedUser(Authentication authentication) {
        return (AppUser) authentication.getPrincipal();
    }
}
