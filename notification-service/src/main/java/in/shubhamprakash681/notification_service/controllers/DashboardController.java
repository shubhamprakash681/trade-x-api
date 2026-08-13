package in.shubhamprakash681.notification_service.controllers;

import in.shubhamprakash681.common_lib.security.JwtPrincipal;
import in.shubhamprakash681.notification_service.dtos.DashboardDtos;
import in.shubhamprakash681.notification_service.services.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping
    DashboardDtos.DashboardResponse dashboard(@AuthenticationPrincipal JwtPrincipal principal) {
        return dashboardService.dashboard(principal);
    }
}
