package in.shubhamprakash681.notification_service.controllers;

import in.shubhamprakash681.common_lib.security.JwtPrincipal;
import in.shubhamprakash681.notification_service.dtos.NotificationDtos;
import in.shubhamprakash681.notification_service.services.UserNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final UserNotificationService userNotificationService;

    @GetMapping
    List<NotificationDtos.NotificationResponse> notifications(
            @AuthenticationPrincipal JwtPrincipal principal,
            @RequestParam(defaultValue = "100") int limit) {
        return userNotificationService.notifications(principal, limit);
    }
}
