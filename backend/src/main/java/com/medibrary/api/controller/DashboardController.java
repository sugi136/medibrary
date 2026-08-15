package com.medibrary.api.controller;

import com.medibrary.api.dto.FavoriteDtos;
import com.medibrary.api.service.DashboardService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public FavoriteDtos.DashboardSummary summary(@AuthenticationPrincipal Long userId) {
        return dashboardService.getSummary(userId);
    }
}
