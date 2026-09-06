package in.shubhamprakash681.portfolio_service.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class MarketDtos {
    private MarketDtos() {
    }

    public record CandleResponse(
            String symbol,
            String interval,
            LocalDateTime candleTime,
            BigDecimal open,
            BigDecimal high,
            BigDecimal low,
            BigDecimal close,
            Long volume) {
    }
}
