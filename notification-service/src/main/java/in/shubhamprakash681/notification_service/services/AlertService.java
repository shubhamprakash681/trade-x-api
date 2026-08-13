package in.shubhamprakash681.notification_service.services;

import in.shubhamprakash681.common_lib.security.JwtPrincipal;
import in.shubhamprakash681.notification_service.clients.MarketClient;
import in.shubhamprakash681.notification_service.dtos.AlertDtos;
import in.shubhamprakash681.notification_service.dtos.PriceTick;
import in.shubhamprakash681.notification_service.dtos.StockResponse;
import in.shubhamprakash681.notification_service.entity.PriceAlert;
import in.shubhamprakash681.notification_service.entity.UserNotification;
import in.shubhamprakash681.notification_service.enums.AlertStatus;
import in.shubhamprakash681.notification_service.repositories.PriceAlertRepository;
import in.shubhamprakash681.notification_service.repositories.UserNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertService {
    private final PriceAlertRepository priceAlertRepository;
    private final UserNotificationRepository userNotificationRepository;
    private final MarketClient marketClient;

    @Transactional(readOnly = true)
    public List<AlertDtos.AlertResponse> alerts(JwtPrincipal principal) {
        return priceAlertRepository.findByUserIdOrderByCreatedAtDesc(principal.userId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public AlertDtos.AlertResponse create(JwtPrincipal principal, AlertDtos.CreateAlertRequest request) {
        StockResponse stock = marketClient.getStock(normalizeSymbol(request.symbol()));

        PriceAlert alert = priceAlertRepository.save(PriceAlert.builder()
                .userId(principal.userId())
                .symbol(stock.symbol())
                .stockName(stock.name())
                .targetPrice(request.targetPrice().setScale(4, RoundingMode.HALF_UP))
                .condition(request.condition())
                .status(AlertStatus.ACTIVE)
                .build());

        return toResponse(alert);
    }

    @Transactional
    public void delete(JwtPrincipal principal, Long id, String symbol) {
        long deleted;
        if (id != null) {
            deleted = priceAlertRepository.deleteByUserIdAndId(principal.userId(), id);
        } else if (symbol != null && !symbol.isBlank()) {
            deleted = priceAlertRepository.deleteByUserIdAndSymbolIn(principal.userId(), List.of(normalizeSymbol(symbol)));
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Either id or symbol is required");
        }

        if (deleted == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Alert not found");
        }
    }

    @Transactional
    public void evaluate(PriceTick tick) {
        if (tick == null || tick.symbol() == null || tick.price() == null) {
            return;
        }

        String symbol = normalizeSymbol(tick.symbol());
        LocalDateTime timestamp = tick.timestamp() == null ? LocalDateTime.now() : tick.timestamp();
        List<PriceAlert> activeAlerts = priceAlertRepository.findBySymbolAndStatus(symbol, AlertStatus.ACTIVE);

        for (PriceAlert alert : activeAlerts) {
            if (alert.isTriggeredBy(tick.price())) {
                alert.markTriggered(tick.price().setScale(4, RoundingMode.HALF_UP), timestamp);
                userNotificationRepository.save(UserNotification.builder()
                        .userId(alert.getUserId())
                        .symbol(alert.getSymbol())
                        .alertId(alert.getId())
                        .title("Price alert triggered")
                        .message(alert.getSymbol() + " is " + alert.getCondition().name().toLowerCase()
                                + " " + alert.getTargetPrice() + " at " + alert.getTriggeredPrice())
                        .build());
            }
        }
    }

    private AlertDtos.AlertResponse toResponse(PriceAlert alert) {
        return new AlertDtos.AlertResponse(
                alert.getId(),
                alert.getSymbol(),
                alert.getStockName(),
                alert.getTargetPrice(),
                alert.getCondition(),
                alert.getStatus(),
                alert.getTriggeredPrice(),
                alert.getTriggeredAt(),
                alert.getCreatedAt());
    }

    private String normalizeSymbol(String symbol) {
        return symbol == null ? "" : symbol.trim().toUpperCase();
    }
}
