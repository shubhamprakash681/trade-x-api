package in.shubhamprakash681.market_service.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class ExternalMarketDtos {
    public record MarketIndexResponse(
            String symbol,
            String name,
            BigDecimal value,
            BigDecimal changeAmount,
            BigDecimal changePercent,
            LocalDateTime asOf) {
    }

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

//    public record MarketSnapshot(
//            List<MarketIndexResponse> indices,
//            List<MarketMoverResponse> gainers,
//            List<MarketMoverResponse> losers,
//            List<MarketTrendResponse> trending) {
//    }
}
