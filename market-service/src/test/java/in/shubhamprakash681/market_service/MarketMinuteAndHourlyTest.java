package in.shubhamprakash681.market_service;

import in.shubhamprakash681.market_service.config.MarketHistoryProperties;
import in.shubhamprakash681.market_service.entity.MarketPriceHistory;
import in.shubhamprakash681.market_service.repositories.MarketPriceHistoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;

import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "eureka.client.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:market_service_minute_test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "tradex.market.history.interval=MINUTE",
        "tradex.market.history.minutes=10",
        "tradex.jwt.issuer=tradex-test",
        "tradex.jwt.secret=tradex-test-secret-that-is-long-enough-for-hmac-signing",
        "tradex.jwt.access-token-minutes=30",
        "tradex.jwt.refresh-token-days=7"
})
class MarketMinuteAndHourlyTest {

    @Autowired
    private MarketHistoryProperties properties;

    @Autowired
    private MarketPriceHistoryRepository repository;

    @Test
    void testMinuteIntervalRunsWithoutCrashing() {
        assertEquals(MarketHistoryProperties.Interval.MINUTE, properties.getInterval());

        List<MarketPriceHistory> candles = repository.findBySymbolAndIntervalOrderByCandleTimeDesc(
                "TCS", "MINUTE", PageRequest.of(0, 50));

        assertFalse(candles.isEmpty(), "Minute candles should be seeded successfully without crashing");

        for (int i = 0; i < candles.size() - 1; i++) {
            MarketPriceHistory newer = candles.get(i);
            MarketPriceHistory older = candles.get(i + 1);

            assertEquals("MINUTE", newer.getInterval());
            long diffMinutes = ChronoUnit.MINUTES.between(older.getCandleTime(), newer.getCandleTime());
            assertEquals(1, diffMinutes);
            assertTrue(newer.getClosePrice().doubleValue() > 0);
            assertTrue(newer.getClosePrice().doubleValue() < 1_000_000, "Price must not overflow");
        }
    }
}

