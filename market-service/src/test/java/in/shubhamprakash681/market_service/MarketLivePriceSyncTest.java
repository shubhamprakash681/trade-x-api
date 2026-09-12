package in.shubhamprakash681.market_service;

import in.shubhamprakash681.market_service.config.MarketHistoryProperties;
import in.shubhamprakash681.market_service.entity.LivePriceTick;
import in.shubhamprakash681.market_service.entity.MarketPriceHistory;
import in.shubhamprakash681.market_service.repositories.LivePriceRepository;
import in.shubhamprakash681.market_service.repositories.MarketPriceHistoryRepository;
import in.shubhamprakash681.market_service.service.HistoricalMarketDataSeeder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "eureka.client.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:market_live_sync_test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "tradex.market.history.interval=SECONDS",
        "tradex.market.history.seconds=10",
        "tradex.jwt.issuer=tradex-test",
        "tradex.jwt.secret=tradex-test-secret-that-is-long-enough-for-hmac-signing",
        "tradex.jwt.access-token-minutes=30",
        "tradex.jwt.refresh-token-days=7"
})
class MarketLivePriceSyncTest {

    @Autowired
    private MarketPriceHistoryRepository marketPriceHistoryRepository;

    @Autowired
    private LivePriceRepository livePriceRepository;

    @Autowired
    private HistoricalMarketDataSeeder seeder;

    @Test
    void testCandleSyncsWithLivePrice() {
        // 1. Given a live price in price_history (e.g. AXISBANK at 2883.5500)
        BigDecimal livePrice = new BigDecimal("2883.5500");
        livePriceRepository.save(new LivePriceTick(null, "AXISBANK", livePrice, LocalDateTime.now()));

        // 2. Force reseed
        seeder.forceReseed();

        // 3. The latest candle for AXISBANK should be aligned with live price (2883.5500)
        Optional<MarketPriceHistory> latestCandle = marketPriceHistoryRepository
                .findFirstBySymbolAndIntervalOrderByCandleTimeDesc("AXISBANK", "SECONDS");

        assertTrue(latestCandle.isPresent(), "Candle should exist");
        BigDecimal close = latestCandle.get().getClosePrice();

        // Check that close price is directly matching the live price (within 0.1%)
        double diff = Math.abs(close.subtract(livePrice).doubleValue());
        assertTrue(diff < 2.0, "Latest candle close (" + close + ") must be within tight range of live price (" + livePrice + ")");
        assertFalse(close.doubleValue() < 600.0, "Close price must not be at the old 498 level");
    }

    @Test
    void testOngoingCandleTracksLivePriceChange() {
        // 1. Initial live price
        BigDecimal initialPrice = new BigDecimal("2883.5500");
        livePriceRepository.save(new LivePriceTick(null, "AXISBANK", initialPrice, LocalDateTime.now()));
        seeder.forceReseed();

        // 2. New realistic live price tick arrives in price_history (within realistic 0.25% tick move)
        BigDecimal newLivePrice = new BigDecimal("2885.0000");
        livePriceRepository.save(new LivePriceTick(null, "AXISBANK", newLivePrice, LocalDateTime.now()));

        // Wait 1.1s so that the next second arrives
        try {
            Thread.sleep(1100);
        } catch (InterruptedException ignored) {}

        // 3. Append ongoing candle
        seeder.appendLatestCandleForSecond();

        // 4. Verify latest candle close tracks the new live price without jump
        Optional<MarketPriceHistory> latestCandle = marketPriceHistoryRepository
                .findFirstBySymbolAndIntervalOrderByCandleTimeDesc("AXISBANK", "SECONDS");

        assertTrue(latestCandle.isPresent());
        assertEquals(newLivePrice.setScale(4, RoundingMode.HALF_UP),
                latestCandle.get().getClosePrice().setScale(4, RoundingMode.HALF_UP),
                "Ongoing candle close must track the new live price");
    }

    @Test
    void testDivergenceDetectionAndAutoPurge() {
        // Set up a live price
        BigDecimal livePrice = new BigDecimal("2883.5500");
        livePriceRepository.save(new LivePriceTick(null, "AXISBANK", livePrice, LocalDateTime.now()));

        // Manually insert an old corrupted candle at 498.0000
        marketPriceHistoryRepository.deleteBySymbolAndInterval("AXISBANK", "SECONDS");
        marketPriceHistoryRepository.save(new MarketPriceHistory(
                "AXISBANK",
                "SECONDS",
                LocalDateTime.now(),
                new BigDecimal("497.0000"),
                new BigDecimal("499.0000"),
                new BigDecimal("496.0000"),
                new BigDecimal("498.0000"),
                1000L
        ));

        // When seeder runs, it should detect 498 is wildly diverged from 2883, purge, and reseed
        seeder.seedMissingHistory();

        Optional<MarketPriceHistory> latestCandle = marketPriceHistoryRepository
                .findFirstBySymbolAndIntervalOrderByCandleTimeDesc("AXISBANK", "SECONDS");

        assertTrue(latestCandle.isPresent());
        assertTrue(latestCandle.get().getClosePrice().doubleValue() > 2000.0,
                "Old corrupted candle (498) should have been purged and reseeded close to live price (2883)");
    }
}
