package in.shubhamprakash681.market_service.service;

import in.shubhamprakash681.market_service.catalog.SupportedStockCatalog;
import in.shubhamprakash681.market_service.config.MarketHistoryProperties;
import in.shubhamprakash681.market_service.dtos.MarketDtos;
import in.shubhamprakash681.market_service.entity.MarketPriceHistory;
import in.shubhamprakash681.market_service.repositories.MarketPriceHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MarketHistoryService {
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private final SupportedStockCatalog supportedStockCatalog;
    private final MarketPriceHistoryRepository marketPriceHistoryRepository;
    private final MarketHistoryProperties properties;
    private final HistoricalMarketDataSeeder historicalMarketDataSeeder;

    @Transactional
    public List<MarketDtos.CandleResponse> history(String symbol, LocalDate from, LocalDate to) {
        String fromStr = from != null ? from.toString() : null;
        String toStr = to != null ? to.toString() : null;
        return history(symbol, null, null, fromStr, toStr);
    }

    @Transactional
    public List<MarketDtos.CandleResponse> history(String symbol, String intervalParam, String rangeParam, String fromParam, String toParam) {
        String normalized = normalizeSupportedSymbol(symbol);
        MarketHistoryProperties.Interval targetInterval = MarketHistoryProperties.parseInterval(intervalParam);
        if (targetInterval == null) {
            targetInterval = properties.getInterval();
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime;
        LocalDateTime startTime;

        LocalDateTime parsedTo = parseDateTimeOrDate(toParam, true);
        LocalDateTime parsedFrom = parseDateTimeOrDate(fromParam, false);

        if (parsedTo != null) {
            endTime = properties.endTime(targetInterval, parsedTo);
        } else {
            endTime = properties.endTime(targetInterval, now);
        }

        if (parsedFrom != null) {
            startTime = properties.startTime(targetInterval, parsedFrom);
        } else if (rangeParam != null && !rangeParam.isBlank()) {
            startTime = calculateStartTimeFromRange(rangeParam.trim().toUpperCase(), now, targetInterval);
        } else {
            startTime = properties.startTime(targetInterval, now);
        }

        startTime = targetInterval.normalizeStart(startTime);
        endTime = targetInterval.normalizeEnd(endTime);

        if (startTime.isAfter(endTime)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "from must be before or equal to to");
        }

        long expected = MarketHistoryProperties.expectedCandleCount(targetInterval, startTime, endTime);
        long count = marketPriceHistoryRepository.countBySymbolAndIntervalAndCandleTimeBetween(
                normalized, targetInterval.name(), startTime, endTime);

        Optional<MarketPriceHistory> oldestCandle = marketPriceHistoryRepository
                .findFirstBySymbolAndIntervalOrderByCandleTimeAsc(normalized, targetInterval.name());

        boolean needsSeeding = false;
        if (count == 0 || oldestCandle.isEmpty()) {
            needsSeeding = true;
        } else if (oldestCandle.get().getCandleTime().isAfter(startTime)) {
            // Existing data does not reach far back enough to cover startTime (e.g. 5Y requested, but DB only has 1Y)
            needsSeeding = true;
        } else if (count < expected * 0.8) {
            needsSeeding = true;
        }

        if (needsSeeding) {
            LocalDateTime fullStart = properties.startTime(targetInterval, now);
            LocalDateTime seedStart = startTime.isBefore(fullStart) ? startTime : fullStart;
            historicalMarketDataSeeder.seedStockHistory(normalized, targetInterval, seedStart, endTime);
        }

        List<MarketPriceHistory> candles = marketPriceHistoryRepository
                .findBySymbolAndIntervalAndCandleTimeBetweenOrderByCandleTimeAsc(
                        normalized, targetInterval.name(), startTime, endTime);

        if (candles.size() > 5000) {
            candles = candles.subList(candles.size() - 5000, candles.size());
        }

        return candles.stream()
                .map(this::toCandleResponse)
                .toList();
    }

    private LocalDateTime calculateStartTimeFromRange(String range, LocalDateTime now, MarketHistoryProperties.Interval interval) {
        return switch (range) {
            case "1D" -> now.minusDays(1);
            case "5D" -> now.minusDays(5);
            case "1M" -> now.minusMonths(1);
            case "3M" -> now.minusMonths(3);
            case "6M" -> now.minusMonths(6);
            case "YTD" -> LocalDate.of(now.getYear(), 1, 1).atStartOfDay();
            case "1Y" -> now.minusYears(1);
            case "5Y" -> now.minusYears(5);
            case "ALL" -> now.minusYears(10);
            default -> properties.startTime(interval, now);
        };
    }

    private LocalDateTime parseDateTimeOrDate(String val, boolean isEnd) {
        if (val == null || val.isBlank()) {
            return null;
        }
        String s = val.trim();
        if (s.matches("^\\d+$")) {
            long num = Long.parseLong(s);
            Instant instant = num > 10_000_000_000L
                    ? Instant.ofEpochMilli(num)
                    : Instant.ofEpochSecond(num);
            return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        }
        if (s.length() == 10) {
            LocalDate date = LocalDate.parse(s);
            return isEnd ? date.atTime(LocalTime.MAX) : date.atStartOfDay();
        }
        return LocalDateTime.parse(s, DateTimeFormatter.ISO_DATE_TIME);
    }

    @Transactional(readOnly = true)
    public MarketDtos.CandleResponse latestCandle(String symbol) {
        String normalized = normalizeSupportedSymbol(symbol);

        return marketPriceHistoryRepository.findFirstBySymbolAndIntervalOrderByCandleTimeDesc(normalized, properties.getInterval().name())
                .map(this::toCandleResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Market history not found"));
    }

    @Transactional(readOnly = true)
    public List<MarketDtos.MarketMoverResponse> gainers() {
        return latestMovers().stream()
                .sorted(Comparator.comparing(MarketDtos.MarketMoverResponse::changePercent).reversed())
                .limit(5)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MarketDtos.MarketMoverResponse> losers() {
        return latestMovers().stream()
                .sorted(Comparator.comparing(MarketDtos.MarketMoverResponse::changePercent))
                .limit(5)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MarketDtos.MarketTrendResponse> trending() {
        return latestMovers().stream()
                .sorted(Comparator.comparing(MarketDtos.MarketMoverResponse::volume).reversed())
                .limit(5)
                .map(mover -> new MarketDtos.MarketTrendResponse(
                        mover.symbol(),
                        mover.name(),
                        mover.price(),
                        mover.changePercent(),
                        trendScore(mover),
                        "High simulated volume and price momentum",
                        mover.asOf()))
                .toList();
    }

    @Transactional(readOnly = true)
    public MarketDtos.MarketStatusResponse status() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = properties.startTime(now);
        LocalDateTime endTime = properties.endTime(now);
        long expectedCandles = properties.expectedCandleCount(startTime, endTime);
        String interval = properties.getInterval().name();

        List<MarketDtos.SymbolStatus> symbolStatuses = supportedStockCatalog.symbols().stream()
                .map(symbol -> {
                    long candles = marketPriceHistoryRepository.countBySymbolAndIntervalAndCandleTimeBetween(symbol, interval, startTime, endTime);
                    LocalDateTime firstTime = marketPriceHistoryRepository.findFirstBySymbolAndIntervalOrderByCandleTimeAsc(symbol, interval)
                            .map(MarketPriceHistory::getCandleTime)
                            .orElse(null);
                    LocalDateTime latestTime = marketPriceHistoryRepository.findFirstBySymbolAndIntervalOrderByCandleTimeDesc(symbol, interval)
                            .map(MarketPriceHistory::getCandleTime)
                            .orElse(null);

                    return new MarketDtos.SymbolStatus(symbol, candles, firstTime, latestTime,
                            candles >= expectedCandles && latestTime != null && !latestTime.isBefore(endTime));
                })
                .toList();

        return new MarketDtos.MarketStatusResponse(startTime, endTime, interval,
                supportedStockCatalog.symbols().size(), symbolStatuses);
    }

    public MarketDtos.MarketStatusResponse regenerateMissingHistory() {
        historicalMarketDataSeeder.forceReseed();
        return status();
    }

    private List<MarketDtos.MarketMoverResponse> latestMovers() {
        Map<String, SupportedStockCatalog.StockSeed> stockBySymbol = supportedStockCatalog.stocks().stream()
                .collect(Collectors.toMap(SupportedStockCatalog.StockSeed::symbol, Function.identity()));

        return marketPriceHistoryRepository.findLatestForSymbols(supportedStockCatalog.symbols(), properties.getInterval().name()).stream()
                .map(candle -> toMover(candle, stockBySymbol.get(candle.getSymbol())))
                .toList();
    }

    private MarketDtos.MarketMoverResponse toMover(MarketPriceHistory candle, SupportedStockCatalog.StockSeed stock) {
        BigDecimal changeAmount = candle.getClosePrice().subtract(candle.getOpenPrice()).setScale(4, RoundingMode.HALF_UP);
        BigDecimal changePercent = candle.getOpenPrice().compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP)
                : changeAmount.multiply(ONE_HUNDRED).divide(candle.getOpenPrice(), 4, RoundingMode.HALF_UP);

        return new MarketDtos.MarketMoverResponse(
                candle.getSymbol(),
                stock.name(),
                candle.getClosePrice(),
                changeAmount,
                changePercent,
                candle.getVolume(),
                candle.getCandleTime());
    }

    private BigDecimal trendScore(MarketDtos.MarketMoverResponse mover) {
        BigDecimal volumeScore = BigDecimal.valueOf(Math.min(70.0, Math.log10(Math.max(10L, mover.volume())) * 10));
        BigDecimal momentumScore = mover.changePercent().abs().multiply(new BigDecimal("4.5"));

        return volumeScore.add(momentumScore).min(new BigDecimal("100")).setScale(4, RoundingMode.HALF_UP);
    }

    private MarketDtos.CandleResponse toCandleResponse(MarketPriceHistory candle) {
        return new MarketDtos.CandleResponse(
                candle.getSymbol(),
                candle.getInterval(),
                candle.getCandleTime(),
                candle.getOpenPrice(),
                candle.getHighPrice(),
                candle.getLowPrice(),
                candle.getClosePrice(),
                candle.getVolume());
    }

    private String normalizeSupportedSymbol(String symbol) {
        String normalized = symbol == null ? "" : symbol.trim().toUpperCase();

        if (supportedStockCatalog.findBySymbol(normalized).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Stock not found");
        }

        return normalized;
    }
}
