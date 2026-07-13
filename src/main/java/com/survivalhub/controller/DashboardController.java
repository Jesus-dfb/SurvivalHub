package com.survivalhub.controller;

import com.survivalhub.model.AppUser;
import com.survivalhub.model.WorldDashboardSummary;
import com.survivalhub.service.DashboardService;
import com.survivalhub.service.WorldService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/worlds/{worldId}/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final WorldService worldService;

    public DashboardController(DashboardService dashboardService, WorldService worldService) {
        this.dashboardService = dashboardService;
        this.worldService = worldService;
    }

    @GetMapping
    public ResponseEntity<WorldDashboardSummary> getWorldDashboard(
            @PathVariable Long worldId,
            Authentication authentication
    ) {
        AppUser user = (AppUser) authentication.getPrincipal();

        if (worldService.getWorldByIdForOwner(worldId, user.getId()).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        WorldDashboardSummary dashboard = dashboardService.getWorldDashboard(worldId);

        return ResponseEntity.ok(dashboard);
    }
}
