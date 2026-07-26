package in.shubhamprakash681.market_service.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PriceTick(
        String symbol,
        BigDecimal price,
        BigDecimal previousPrice,
        BigDecimal changeAmount,
        BigDecimal changePercent,
        boolean synthetic,
        LocalDateTime timestamp
) {
}
