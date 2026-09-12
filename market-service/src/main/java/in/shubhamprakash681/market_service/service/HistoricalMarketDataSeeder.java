package in.shubhamprakash681.market_service.service;

import in.shubhamprakash681.market_service.catalog.SupportedStockCatalog;
import in.shubhamprakash681.market_service.config.MarketHistoryProperties;
import in.shubhamprakash681.market_service.entity.MarketPriceHistory;
import in.shubhamprakash681.market_service.repositories.MarketPriceHistoryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class HistoricalMarketDataSeeder implements SmartInitializingSingleton {
    private static final BigDecimal MIN_PRICE = new BigDecimal("1.0000");
    private static final int BATCH_SIZE = 1000;

    private final SupportedStockCatalog supportedStockCatalog;
    private final MarketPriceHistoryRepository marketPriceHistoryRepository;
    private final MarketHistoryProperties properties;
    private final LivePriceService livePriceService;
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

    public void forceReseed() {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            for (SupportedStockCatalog.StockSeed stock : supportedStockCatalog.stocks()) {
                marketPriceHistoryRepository.deleteBySymbolAndInterval(stock.symbol(), properties.getInterval().name());
            }
            marketPriceHistoryRepository.flush();
            if (entityManager != null) {
                entityManager.clear();
            }
            seedMissingHistoryInTransaction();
        });
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

    public synchronized void appendLatestCandleForSecond() {
        LocalDateTime now = properties.getInterval().normalizeEnd(LocalDateTime.now());
        List<MarketPriceHistory> pending = new ArrayList<>();
        Random random = new Random();

        for (SupportedStockCatalog.StockSeed stock : supportedStockCatalog.stocks()) {
            Optional<MarketPriceHistory> latest = marketPriceHistoryRepository
                    .findFirstBySymbolAndIntervalOrderByCandleTimeDesc(stock.symbol(), properties.getInterval().name());

            LocalDateTime nextTime = latest.map(h -> properties.getInterval().next(h.getCandleTime()))
                    .orElse(now);

            if (!nextTime.isAfter(now) && !marketPriceHistoryRepository.existsBySymbolAndIntervalAndCandleTime(stock.symbol(), properties.getInterval().name(), nextTime)) {
                BigDecimal previousClose = latest.map(MarketPriceHistory::getClosePrice)
                        .orElse(stock.referencePrice());

                BigDecimal livePrice = livePriceService.getLivePrice(stock.symbol(), previousClose);

                // If gap between previousClose and livePrice is huge (>5%), history is out of sync.
                // Reseed to realign seamlessly instead of creating a sharp jump.
                double gap = Math.abs(livePrice.subtract(previousClose).doubleValue()) / Math.max(1.0, previousClose.doubleValue());
                if (gap > 0.05) {
                    log.info("Large gap detected between candle and live price for {} (prevClose={}, live={}, gap={}%). Reseeding history.",
                            stock.symbol(), previousClose, livePrice, String.format("%.2f", gap * 100));
                    marketPriceHistoryRepository.deleteBySymbolAndInterval(stock.symbol(), properties.getInterval().name());
                    marketPriceHistoryRepository.flush();
                    if (entityManager != null) {
                        entityManager.clear();
                    }
                    seedMissingHistory(stock, properties.getInterval().name(), properties.startTime(now), now);
                    continue;
                }

                BigDecimal open = previousClose;
                BigDecimal close;
                if (livePrice.compareTo(previousClose) != 0) {
                    // Rate-limit max change per second to 0.25% to ensure smooth realistic transitions
                    BigDecimal maxStep = previousClose.multiply(new BigDecimal("0.0025")).setScale(4, RoundingMode.HALF_UP);
                    BigDecimal delta = livePrice.subtract(previousClose);
                    if (delta.abs().compareTo(maxStep) > 0) {
                        close = previousClose.add(delta.signum() > 0 ? maxStep : maxStep.negate());
                    } else {
                        close = livePrice;
                    }
                } else {
                    double microReturn = (random.nextDouble() - 0.5) * 0.0004;
                    close = previousClose.multiply(BigDecimal.valueOf(1 + microReturn)).setScale(4, RoundingMode.HALF_UP);
                }

                double dt = properties.getInterval().timeStepInDays();
                BigDecimal maxPrice = stock.referencePrice().multiply(new BigDecimal("100")).setScale(4, RoundingMode.HALF_UP);
                BigDecimal spread = close.multiply(BigDecimal.valueOf(0.0002 + random.nextDouble() * 0.0004)).setScale(4, RoundingMode.HALF_UP);
                BigDecimal high = open.max(close).add(spread).min(maxPrice).setScale(4, RoundingMode.HALF_UP);
                BigDecimal low = open.min(close).subtract(spread).max(MIN_PRICE).setScale(4, RoundingMode.HALF_UP);
                long volume = volume(stock, random, Math.abs(close.subtract(open).doubleValue() / open.doubleValue()), dt);

                pending.add(new MarketPriceHistory(
                        stock.symbol(),
                        properties.getInterval().name(),
                        nextTime,
                        open,
                        high,
                        low,
                        close,
                        volume));
            }
        }

        if (!pending.isEmpty()) {
            try {
                marketPriceHistoryRepository.saveAll(pending);
                marketPriceHistoryRepository.flush();
                if (entityManager != null) {
                    entityManager.clear();
                }
            } catch (org.springframework.dao.DataIntegrityViolationException e) {
                log.debug("Duplicate candle detected during concurrent append, ignoring: {}", e.getMessage());
            }
        }
    }

    private void seedMissingHistory(SupportedStockCatalog.StockSeed stock,
                                    String interval,
                                    LocalDateTime startTime,
                                    LocalDateTime endTime) {
        BigDecimal targetPrice = livePriceService.getLivePrice(stock.symbol(), stock.referencePrice());
        long expected = properties.expectedCandleCount(startTime, endTime);
        long existingCount = marketPriceHistoryRepository.countBySymbolAndIntervalAndCandleTimeBetween(stock.symbol(), interval, startTime, endTime);

        Optional<MarketPriceHistory> latestCandle = marketPriceHistoryRepository.findFirstBySymbolAndIntervalOrderByCandleTimeDesc(stock.symbol(), interval);
        Optional<MarketPriceHistory> oldestCandle = marketPriceHistoryRepository.findFirstBySymbolAndIntervalOrderByCandleTimeAsc(stock.symbol(), interval);

        boolean diverged = false;
        if (latestCandle.isPresent()) {
            BigDecimal latestClose = latestCandle.get().getClosePrice();
            double ratio = latestClose.divide(targetPrice, 4, RoundingMode.HALF_UP).doubleValue();
            if (properties.getInterval() == MarketHistoryProperties.Interval.SECONDS && (ratio < 0.95 || ratio > 1.05)) {
                diverged = true;
            } else if (properties.getInterval() == MarketHistoryProperties.Interval.MINUTE && (ratio < 0.90 || ratio > 1.10)) {
                diverged = true;
            } else if (ratio < 0.60 || ratio > 1.50) {
                diverged = true;
            }
        }
        if (!diverged && oldestCandle.isPresent()) {
            BigDecimal oldestClose = oldestCandle.get().getClosePrice();
            double oldestRatio = oldestClose.divide(targetPrice, 4, RoundingMode.HALF_UP).doubleValue();
            if (properties.getInterval() == MarketHistoryProperties.Interval.SECONDS && (oldestRatio < 0.90 || oldestRatio > 1.10)) {
                diverged = true;
            } else if (properties.getInterval() == MarketHistoryProperties.Interval.MINUTE && (oldestRatio < 0.85 || oldestRatio > 1.15)) {
                diverged = true;
            }
        }

        if (diverged) {
            log.info("Historical candles for {} ({}) diverged significantly from target price {}. Purging and reseeding.",
                    stock.symbol(), interval, targetPrice);
            marketPriceHistoryRepository.deleteBySymbolAndInterval(stock.symbol(), interval);
            marketPriceHistoryRepository.flush();
            if (entityManager != null) {
                entityManager.clear();
            }
            existingCount = 0;
        }

        if (existingCount >= expected) {
            return;
        }

        Set<LocalDateTime> existingTimes = marketPriceHistoryRepository.findExistingTimes(stock.symbol(), interval, startTime, endTime);
        List<MarketPriceHistory> pending = new ArrayList<>(BATCH_SIZE);

        BigDecimal startRatio = switch (properties.getInterval()) {
            case SECONDS -> new BigDecimal("0.9980");
            case MINUTE -> new BigDecimal("0.9900");
            case HOURLY -> new BigDecimal("0.9500");
            case DAILY, WEEKLY, MONTHLY -> new BigDecimal("0.4200");
        };
        BigDecimal previousClose = targetPrice.multiply(startRatio).setScale(4, RoundingMode.HALF_UP);

        Random random = new Random(stock.symbol().hashCode());
        LocalDateTime candleTime = startTime;
        LocalDateTime lastCandleTime = null;
        long remainingSteps = expected;

        while (!candleTime.isAfter(endTime)) {
            GeneratedCandle candle = nextCandle(stock, candleTime.toLocalDate(),
                    ChronoUnit.DAYS.between(startTime.toLocalDate(), candleTime.toLocalDate()),
                    previousClose, targetPrice, remainingSteps, random, properties.getInterval());
            previousClose = candle.close();
            remainingSteps--;

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
            GeneratedCandle candle = nextCandle(stock, endTime.toLocalDate(),
                    ChronoUnit.DAYS.between(startTime.toLocalDate(), endTime.toLocalDate()),
                    previousClose, targetPrice, 1, random, properties.getInterval());
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
                                       BigDecimal targetPrice,
                                       long remainingSteps,
                                       Random random,
                                       MarketHistoryProperties.Interval interval) {
        double dt = interval.timeStepInDays();
        double sqrtDt = Math.sqrt(dt);

        BigDecimal maxPrice = stock.referencePrice().multiply(new BigDecimal("100")).setScale(4, RoundingMode.HALF_UP);

        if (remainingSteps <= 1) {
            BigDecimal open = previousClose;
            BigDecimal close = targetPrice;
            BigDecimal spread = targetPrice.multiply(BigDecimal.valueOf(Math.max(0.0002, 0.001 * sqrtDt))).setScale(4, RoundingMode.HALF_UP);
            BigDecimal high = open.max(close).add(spread).min(maxPrice).setScale(4, RoundingMode.HALF_UP);
            BigDecimal low = open.min(close).subtract(spread).max(MIN_PRICE).setScale(4, RoundingMode.HALF_UP);
            long volume = volume(stock, random, 0.001, dt);
            return new GeneratedCandle(open, high, low, close, volume);
        }

        double currentVal = previousClose.doubleValue();
        double targetVal = targetPrice.doubleValue();
        double guidedDrift = (targetVal - currentVal) / (currentVal * remainingSteps);

        double trend = (stock.sector().equals("ETF") ? 0.00023 : 0.00031) * dt;
        double sectorCycle = Math.sin(dayIndex / 145.0 + stock.symbol().length()) * 0.0045 * dt;
        double shock = random.nextGaussian() * (stock.sector().equals("ETF") ? 0.008 : 0.014) * sqrtDt;
        double crash = crashFactor(date) * dt;
        double stepReturn = guidedDrift + trend + sectorCycle + shock + crash;

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
