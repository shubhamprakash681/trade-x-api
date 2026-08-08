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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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

    @Transactional(readOnly = true)
    public List<MarketDtos.CandleResponse> history(String symbol, LocalDate from, LocalDate to) {
        String normalized = normalizeSupportedSymbol(symbol);
        LocalDate today = LocalDate.now();
        LocalDate endDate = to == null ? today : to;
        LocalDate startDate = from == null ? properties.startDate(endDate) : from;

        if (startDate.isAfter(endDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "from must be before or equal to to");
        }

        LocalDateTime startTime = startDate.atStartOfDay();
        LocalDateTime endTime = endDate.equals(today)
                ? properties.endTime(LocalDateTime.now())
                : properties.endTime(endDate.atTime(LocalTime.MAX));

        return marketPriceHistoryRepository
                .findBySymbolAndIntervalAndCandleTimeBetweenOrderByCandleTimeAsc(normalized, properties.getInterval().name(), startTime, endTime)
                .stream()
                .map(this::toCandleResponse)
                .toList();
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
        LocalDate today = LocalDate.now();
        LocalDateTime startTime = properties.startTime(today);
        LocalDateTime endTime = properties.endTime(LocalDateTime.now());
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
        historicalMarketDataSeeder.seedMissingHistory();
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
