package in.shubhamprakash681.notification_service.controllers;

import in.shubhamprakash681.common_lib.security.JwtPrincipal;
import in.shubhamprakash681.notification_service.dtos.AlertDtos;
import in.shubhamprakash681.notification_service.services.AlertService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {
    private final AlertService alertService;

    @GetMapping
    List<AlertDtos.AlertResponse> alerts(@AuthenticationPrincipal JwtPrincipal principal) {
        return alertService.alerts(principal);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    AlertDtos.AlertResponse create(@AuthenticationPrincipal JwtPrincipal principal,
            @Valid @RequestBody AlertDtos.CreateAlertRequest request) {
        return alertService.create(principal, request);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@AuthenticationPrincipal JwtPrincipal principal,
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String symbol) {
        alertService.delete(principal, id, symbol);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteById(@AuthenticationPrincipal JwtPrincipal principal, @PathVariable Long id) {
        alertService.delete(principal, id, null);
    }
}
