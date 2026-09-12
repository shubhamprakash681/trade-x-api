package in.shubhamprakash681.market_service;

import in.shubhamprakash681.market_service.config.MarketHistoryProperties;
import in.shubhamprakash681.market_service.entity.MarketPriceHistory;
import in.shubhamprakash681.market_service.repositories.MarketPriceHistoryRepository;
import in.shubhamprakash681.market_service.service.HistoricalMarketDataSeeder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "eureka.client.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:market_service_seconds_test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "tradex.market.history.interval=SECONDS",
        "tradex.market.history.seconds=10",
        "tradex.jwt.issuer=tradex-test",
        "tradex.jwt.secret=tradex-test-secret-that-is-long-enough-for-hmac-signing",
        "tradex.jwt.access-token-minutes=30",
        "tradex.jwt.refresh-token-days=7"
})
class MarketIntervalTest {

    @Autowired
    private MarketHistoryProperties properties;

    @Autowired
    private MarketPriceHistoryRepository repository;

    @Autowired
    private HistoricalMarketDataSeeder seeder;

    @Test
    void testSecondsIntervalConfigurationAndSeeding() {
        assertEquals(MarketHistoryProperties.Interval.SECONDS, properties.getInterval());

        List<MarketPriceHistory> candles = repository.findBySymbolAndIntervalOrderByCandleTimeDesc(
                "RELIANCE", "SECONDS", PageRequest.of(0, 50));

        assertFalse(candles.isEmpty(), "Candles should be generated with interval SECONDS");

        for (int i = 0; i < candles.size() - 1; i++) {
            MarketPriceHistory newer = candles.get(i);
            MarketPriceHistory older = candles.get(i + 1);

            assertEquals("SECONDS", newer.getInterval());
            long diffSeconds = ChronoUnit.SECONDS.between(older.getCandleTime(), newer.getCandleTime());
            assertEquals(1, diffSeconds, "Candles must be generated with each second delay (1 second apart)");
            assertTrue(newer.getClosePrice().doubleValue() > 0, "Price must be positive");
            assertTrue(newer.getClosePrice().doubleValue() < 1_000_000, "Price must be bounded (no overflow)");
        }
    }

    @Test
    void testSecondsIntervalEnumBehavior() {
        LocalDateTime time = LocalDateTime.of(2026, 9, 12, 10, 15, 30, 500_000_000);

        LocalDateTime next = MarketHistoryProperties.Interval.SECONDS.next(time);
        assertEquals(time.plusSeconds(1), next);

        LocalDateTime normalized = MarketHistoryProperties.Interval.SECONDS.normalizeEnd(time);
        assertEquals(LocalDateTime.of(2026, 9, 12, 10, 15, 30), normalized);

        assertEquals(1.0 / 86400.0, MarketHistoryProperties.Interval.SECONDS.timeStepInDays());

        LocalDateTime start = LocalDateTime.of(2026, 9, 12, 10, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 9, 12, 10, 0, 10);
        long count = properties.expectedCandleCount(start, end);
        assertEquals(11, count);
    }

    @Test
    void testAppendLatestCandleForSecond() {
        long initialCount = repository.count();
        seeder.appendLatestCandleForSecond();
        // Should either add candles or be at latest
        long newCount = repository.count();
        assertTrue(newCount >= initialCount);
    }
}

