package in.shubhamprakash681.market_service.service;

import in.shubhamprakash681.market_service.catalog.SupportedStockCatalog;
import in.shubhamprakash681.market_service.config.MarketHistoryProperties;
import in.shubhamprakash681.market_service.entity.MarketPriceHistory;
import in.shubhamprakash681.market_service.repositories.MarketPriceHistoryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class HistoricalMarketDataSeeder implements SmartInitializingSingleton {
    private static final BigDecimal MIN_PRICE = new BigDecimal("1.0000");
    private static final int BATCH_SIZE = 1000;

    private final SupportedStockCatalog supportedStockCatalog;
    private final MarketPriceHistoryRepository marketPriceHistoryRepository;
    private final MarketHistoryProperties properties;
    private final PlatformTransactionManager transactionManager;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void afterSingletonsInstantiated() {
        seedMissingHistory();
    }

    public void seedMissingHistory() {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> seedMissingHistoryInTransaction());
    }

    private void seedMissingHistoryInTransaction() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = properties.startTime(now);
        LocalDateTime endTime = properties.endTime(now);
        String interval = properties.getInterval().name();

        for (SupportedStockCatalog.StockSeed stock : supportedStockCatalog.stocks()) {
            seedMissingHistory(stock, interval, startTime, endTime);
        }
    }

    @Scheduled(fixedDelayString = "${tradex.market.history.generation-delay-ms:1000}")
    public void generateOngoingCandle() {
        if (properties.getInterval() == MarketHistoryProperties.Interval.SECONDS) {
            new TransactionTemplate(transactionManager).executeWithoutResult(status -> appendLatestCandleForSecond());
        }
    }

    public void appendLatestCandleForSecond() {
        LocalDateTime now = properties.getInterval().normalizeEnd(LocalDateTime.now());
        List<MarketPriceHistory> pending = new ArrayList<>();

        for (SupportedStockCatalog.StockSeed stock : supportedStockCatalog.stocks()) {
            Optional<MarketPriceHistory> latest = marketPriceHistoryRepository
                    .findFirstBySymbolAndIntervalOrderByCandleTimeDesc(stock.symbol(), properties.getInterval().name());

            LocalDateTime nextTime = latest.map(h -> properties.getInterval().next(h.getCandleTime()))
                    .orElse(now);

            if (!nextTime.isAfter(now)) {
                BigDecimal previousClose = latest.map(MarketPriceHistory::getClosePrice)
                        .orElse(stock.referencePrice());

                GeneratedCandle candle = nextCandle(stock, nextTime.toLocalDate(),
                        0, previousClose, new Random(), properties.getInterval());

                pending.add(new MarketPriceHistory(
                        stock.symbol(),
                        properties.getInterval().name(),
                        nextTime,
                        candle.open(),
                        candle.high(),
                        candle.low(),
                        candle.close(),
                        candle.volume()));
            }
        }

        if (!pending.isEmpty()) {
            marketPriceHistoryRepository.saveAll(pending);
            marketPriceHistoryRepository.flush();
            if (entityManager != null) {
                entityManager.clear();
            }
        }
    }

    private void seedMissingHistory(SupportedStockCatalog.StockSeed stock,
                                    String interval,
                                    LocalDateTime startTime,
                                    LocalDateTime endTime) {
        long expected = properties.expectedCandleCount(startTime, endTime);
        long existingCount = marketPriceHistoryRepository.countBySymbolAndIntervalAndCandleTimeBetween(stock.symbol(), interval, startTime, endTime);
        if (existingCount >= expected) {
            return;
        }

        Set<LocalDateTime> existingTimes = marketPriceHistoryRepository.findExistingTimes(stock.symbol(), interval, startTime, endTime);
        List<MarketPriceHistory> pending = new ArrayList<>(BATCH_SIZE);
        BigDecimal previousClose = stock.referencePrice().multiply(new BigDecimal("0.42")).setScale(4, RoundingMode.HALF_UP);
        Random random = new Random(stock.symbol().hashCode());
        LocalDateTime candleTime = startTime;
        LocalDateTime lastCandleTime = null;

        while (!candleTime.isAfter(endTime)) {
            GeneratedCandle candle = nextCandle(stock, candleTime.toLocalDate(), ChronoUnit.DAYS.between(startTime.toLocalDate(), candleTime.toLocalDate()), previousClose, random, properties.getInterval());
            previousClose = candle.close();

            if (!existingTimes.contains(candleTime)) {
                pending.add(new MarketPriceHistory(
                        stock.symbol(),
                        interval,
                        candleTime,
                        candle.open(),
                        candle.high(),
                        candle.low(),
                        candle.close(),
                        candle.volume()));
            }

            if (pending.size() == BATCH_SIZE) {
                marketPriceHistoryRepository.saveAll(pending);
                marketPriceHistoryRepository.flush();
                if (entityManager != null) {
                    entityManager.clear();
                }
                pending.clear();
            }

            lastCandleTime = candleTime;
            candleTime = properties.getInterval().next(candleTime);
        }

        if (lastCandleTime != null && !lastCandleTime.equals(endTime)) {
            GeneratedCandle candle = nextCandle(stock, endTime.toLocalDate(), ChronoUnit.DAYS.between(startTime.toLocalDate(), endTime.toLocalDate()), previousClose, random, properties.getInterval());
            if (!existingTimes.contains(endTime)) {
                pending.add(new MarketPriceHistory(
                        stock.symbol(),
                        interval,
                        endTime,
                        candle.open(),
                        candle.high(),
                        candle.low(),
                        candle.close(),
                        candle.volume()));
            }
        }

        if (!pending.isEmpty()) {
            marketPriceHistoryRepository.saveAll(pending);
            marketPriceHistoryRepository.flush();
            if (entityManager != null) {
                entityManager.clear();
            }
        }
    }

    private GeneratedCandle nextCandle(SupportedStockCatalog.StockSeed stock,
                                       LocalDate date,
                                       long dayIndex,
                                       BigDecimal previousClose,
                                       Random random,
                                       MarketHistoryProperties.Interval interval) {
        double dt = interval.timeStepInDays();
        double sqrtDt = Math.sqrt(dt);

        double trend = (stock.sector().equals("ETF") ? 0.00023 : 0.00031) * dt;
        double sectorCycle = Math.sin(dayIndex / 145.0 + stock.symbol().length()) * 0.0045 * dt;
        double shock = random.nextGaussian() * (stock.sector().equals("ETF") ? 0.008 : 0.014) * sqrtDt;
        double crash = crashFactor(date) * dt;
        double stepReturn = trend + sectorCycle + shock + crash;

        BigDecimal maxPrice = stock.referencePrice().multiply(new BigDecimal("100")).setScale(4, RoundingMode.HALF_UP);

        BigDecimal open = previousClose.multiply(BigDecimal.valueOf(1 + random.nextGaussian() * 0.003 * sqrtDt))
                .max(MIN_PRICE)
                .min(maxPrice)
                .setScale(4, RoundingMode.HALF_UP);
        BigDecimal close = previousClose.multiply(BigDecimal.valueOf(1 + stepReturn))
                .max(MIN_PRICE)
                .min(maxPrice)
                .setScale(4, RoundingMode.HALF_UP);
        BigDecimal intradaySpread = previousClose.multiply(BigDecimal.valueOf((0.006 + Math.abs(random.nextGaussian()) * 0.012) * sqrtDt))
                .setScale(4, RoundingMode.HALF_UP);
        BigDecimal high = open.max(close).add(intradaySpread).min(maxPrice).setScale(4, RoundingMode.HALF_UP);
        BigDecimal low = open.min(close).subtract(intradaySpread).max(MIN_PRICE).setScale(4, RoundingMode.HALF_UP);
        long volume = volume(stock, random, Math.abs(stepReturn), dt);

        return new GeneratedCandle(open, high, low, close, volume);
    }

    private double crashFactor(LocalDate date) {
        double covidCrash = eventMove(date, LocalDate.of(2020, 2, 20), LocalDate.of(2020, 3, 25), -0.026);
        double covidRecovery = eventMove(date, LocalDate.of(2020, 3, 26), LocalDate.of(2020, 8, 31), 0.0065);
        double inflationShock = eventMove(date, LocalDate.of(2022, 1, 15), LocalDate.of(2022, 6, 30), -0.0045);
        double recovery = eventMove(date, LocalDate.of(2023, 4, 1), LocalDate.of(2024, 1, 31), 0.0025);

        return covidCrash + covidRecovery + inflationShock + recovery;
    }

    private double eventMove(LocalDate date, LocalDate start, LocalDate end, double impact) {
        if (date.isBefore(start) || date.isAfter(end)) {
            return 0.0;
        }

        long totalDays = Math.max(1, ChronoUnit.DAYS.between(start, end));
        long elapsed = ChronoUnit.DAYS.between(start, date);
        double fade = 1.0 - (double) elapsed / totalDays;

        return impact * Math.max(0.20, fade);
    }

    private long volume(SupportedStockCatalog.StockSeed stock, Random random, double absoluteReturn, double dt) {
        long baseVolume = switch (stock.symbol()) {
            case "RELIANCE", "SBIN", "ITC", "NIFTYBEES", "BANKBEES" -> 5_000_000L;
            case "HDFCBANK", "ICICIBANK", "AXISBANK", "BHARTIARTL" -> 3_200_000L;
            default -> 1_300_000L;
        };
        double multiplier = 0.65 + random.nextDouble() * 0.95 + Math.min(2.0, absoluteReturn * 18);

        return Math.max(10L, Math.round(baseVolume * multiplier * dt));
    }

    private record GeneratedCandle(BigDecimal open,
                                   BigDecimal high,
                                   BigDecimal low,
                                   BigDecimal close,
                                   Long volume) {
    }
}
