package in.shubhamprakash681.notification_service.services;

import in.shubhamprakash681.common_lib.security.JwtPrincipal;
import in.shubhamprakash681.notification_service.clients.MarketClient;
import in.shubhamprakash681.notification_service.dtos.DashboardDtos;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private static final int DASHBOARD_NOTIFICATION_LIMIT = 10;

    private final WatchlistService watchlistService;
    private final AlertService alertService;
    private final UserNotificationService userNotificationService;
    private final MarketClient marketClient;

    public DashboardDtos.DashboardResponse dashboard(JwtPrincipal principal) {
        return new DashboardDtos.DashboardResponse(
                watchlistService.watchlist(principal),
                alertService.alerts(principal),
                userNotificationService.notifications(principal, DASHBOARD_NOTIFICATION_LIMIT),
                marketClient.gainers(),
                marketClient.losers(),
                marketClient.trending());
    }
}
