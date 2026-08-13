package in.shubhamprakash681.notification_service.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MarketDtos {
    public record MarketMoverResponse(
            String symbol,
            String name,
            BigDecimal price,
            BigDecimal changeAmount,
            BigDecimal changePercent,
            Long volume,
            LocalDateTime asOf) {
    }

    public record MarketTrendResponse(
            String symbol,
            String name,
            BigDecimal price,
            BigDecimal changePercent,
            BigDecimal score,
            String reason,
            LocalDateTime asOf) {
    }
}
