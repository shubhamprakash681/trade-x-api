package in.shubhamprakash681.price_stream_service.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PriceResponse(
        String symbol,
        BigDecimal price,
        BigDecimal previousPrice,
        BigDecimal changeAmount,
        BigDecimal changePercent,
        boolean synthetic,
        LocalDateTime timestamp
) {
}
