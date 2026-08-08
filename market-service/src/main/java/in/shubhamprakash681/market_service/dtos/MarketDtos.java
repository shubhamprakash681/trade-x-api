package in.shubhamprakash681.market_service.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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

    public record MarketStatusResponse(
            LocalDateTime startTime,
            LocalDateTime endTime,
            String interval,
            int supportedSymbols,
            List<SymbolStatus> symbols) {
    }

    public record SymbolStatus(
            String symbol,
            long candles,
            LocalDateTime firstTime,
            LocalDateTime latestTime,
            boolean complete) {
    }
}
