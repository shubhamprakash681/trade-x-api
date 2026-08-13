package in.shubhamprakash681.notification_service.controllers;

import in.shubhamprakash681.common_lib.security.JwtPrincipal;
import in.shubhamprakash681.notification_service.dtos.WatchlistDtos;
import in.shubhamprakash681.notification_service.services.WatchlistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/watchlist")
@RequiredArgsConstructor
public class WatchlistController {
    private final WatchlistService watchlistService;

    @GetMapping
    List<WatchlistDtos.WatchlistResponse> watchlist(@AuthenticationPrincipal JwtPrincipal principal) {
        return watchlistService.watchlist(principal);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    WatchlistDtos.WatchlistResponse add(@AuthenticationPrincipal JwtPrincipal principal,
            @Valid @RequestBody WatchlistDtos.AddWatchlistRequest request) {
        return watchlistService.add(principal, request);
    }

    @DeleteMapping("/{symbol}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void remove(@AuthenticationPrincipal JwtPrincipal principal, @PathVariable String symbol) {
        watchlistService.remove(principal, symbol);
    }
}
