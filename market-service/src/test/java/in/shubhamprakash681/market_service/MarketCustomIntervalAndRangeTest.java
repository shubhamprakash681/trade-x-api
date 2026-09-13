package in.shubhamprakash681.market_service;

import in.shubhamprakash681.market_service.config.MarketHistoryProperties;
import in.shubhamprakash681.market_service.dtos.MarketDtos;
import in.shubhamprakash681.market_service.service.MarketHistoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "eureka.client.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:market_custom_test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "tradex.market.history.interval=DAILY",
        "tradex.market.history.days=30",
        "tradex.jwt.issuer=tradex-test",
        "tradex.jwt.secret=tradex-test-secret-that-is-long-enough-for-hmac-signing",
        "tradex.jwt.access-token-minutes=30",
        "tradex.jwt.refresh-token-days=7"
})
class MarketCustomIntervalAndRangeTest {

    @Autowired
    private MarketHistoryService marketHistoryService;

    @Test
    void testParseInterval() {
        assertEquals(MarketHistoryProperties.Interval.SECONDS, MarketHistoryProperties.parseInterval("1s"));
        assertEquals(MarketHistoryProperties.Interval.SECONDS, MarketHistoryProperties.parseInterval("SECONDS"));
        assertEquals(MarketHistoryProperties.Interval.MINUTE, MarketHistoryProperties.parseInterval("1m"));
        assertEquals(MarketHistoryProperties.Interval.MINUTE, MarketHistoryProperties.parseInterval("minute"));
        assertEquals(MarketHistoryProperties.Interval.HOURLY, MarketHistoryProperties.parseInterval("1h"));
        assertEquals(MarketHistoryProperties.Interval.HOURLY, MarketHistoryProperties.parseInterval("HOURLY"));
        assertEquals(MarketHistoryProperties.Interval.DAILY, MarketHistoryProperties.parseInterval("D"));
        assertEquals(MarketHistoryProperties.Interval.DAILY, MarketHistoryProperties.parseInterval("1D"));
        assertEquals(MarketHistoryProperties.Interval.DAILY, MarketHistoryProperties.parseInterval("DAILY"));
        assertEquals(MarketHistoryProperties.Interval.WEEKLY, MarketHistoryProperties.parseInterval("W"));
        assertEquals(MarketHistoryProperties.Interval.WEEKLY, MarketHistoryProperties.parseInterval("1W"));
        assertEquals(MarketHistoryProperties.Interval.WEEKLY, MarketHistoryProperties.parseInterval("WEEKLY"));
        assertEquals(MarketHistoryProperties.Interval.MONTHLY, MarketHistoryProperties.parseInterval("M"));
        assertEquals(MarketHistoryProperties.Interval.MONTHLY, MarketHistoryProperties.parseInterval("1M"));
        assertEquals(MarketHistoryProperties.Interval.MONTHLY, MarketHistoryProperties.parseInterval("MONTHLY"));
    }

    @Test
    void testQueryWithCustomIntervalAndRange() {
        // Query daily default
        List<MarketDtos.CandleResponse> dailyCandles = marketHistoryService.history("RELIANCE", "DAILY", "1M", null, null);
        assertNotNull(dailyCandles);
        assertFalse(dailyCandles.isEmpty(), "Daily candles should be returned for 1M range");
        for (MarketDtos.CandleResponse c : dailyCandles) {
            assertEquals("DAILY", c.interval());
            assertEquals("RELIANCE", c.symbol());
            assertTrue(c.close().doubleValue() > 0);
            assertNotNull(c.volume());
        }

        // Query hourly on demand
        List<MarketDtos.CandleResponse> hourlyCandles = marketHistoryService.history("RELIANCE", "HOURLY", "5D", null, null);
        assertNotNull(hourlyCandles);
        assertFalse(hourlyCandles.isEmpty(), "Hourly candles should be generated and returned on demand for 5D range");
        for (MarketDtos.CandleResponse c : hourlyCandles) {
            assertEquals("HOURLY", c.interval());
        }

        // Query with custom from and to
        LocalDate to = LocalDate.now();
        LocalDate from = to.minusDays(10);
        List<MarketDtos.CandleResponse> customDateCandles = marketHistoryService.history("RELIANCE", "DAILY", null, from.toString(), to.toString());
        assertNotNull(customDateCandles);
        assertFalse(customDateCandles.isEmpty());

        // Test 5Y range: must start ~5 years ago, NOT just 1 year ago
        List<MarketDtos.CandleResponse> fiveYearCandles = marketHistoryService.history("RELIANCE", "DAILY", "5Y", null, null);
        assertNotNull(fiveYearCandles);
        assertFalse(fiveYearCandles.isEmpty());
        assertTrue(fiveYearCandles.size() > 1500, "5Y range must have > 1500 daily candles, found: " + fiveYearCandles.size());
        LocalDate fiveYearStart = fiveYearCandles.get(0).candleTime().toLocalDate();
        assertTrue(fiveYearStart.isBefore(LocalDate.now().minusYears(4)), "5Y start date must be at least 4-5 years ago, but was: " + fiveYearStart);

        // Test 1Y range: must start ~1 year ago
        List<MarketDtos.CandleResponse> oneYearCandles = marketHistoryService.history("RELIANCE", "DAILY", "1Y", null, null);
        assertNotNull(oneYearCandles);
        assertTrue(oneYearCandles.size() >= 360 && oneYearCandles.size() <= 370, "1Y range should have ~365 daily candles, found: " + oneYearCandles.size());
        LocalDate oneYearStart = oneYearCandles.get(0).candleTime().toLocalDate();
        assertTrue(oneYearStart.isAfter(LocalDate.now().minusYears(2)), "1Y start date should be ~1 year ago");
    }

    @Test
    void testLargeIntervalRangeComboIsBoundedAndFast() {
        long start = System.currentTimeMillis();

        // 1. SECONDS with 5Y range: must be clamped to at most 3600 candles and complete very quickly (< 3s)
        List<MarketDtos.CandleResponse> seconds5Y = marketHistoryService.history("RELIANCE", "1s", "5Y", null, null);
        long duration = System.currentTimeMillis() - start;

        assertNotNull(seconds5Y);
        assertFalse(seconds5Y.isEmpty());
        assertTrue(seconds5Y.size() <= 3601, "SECONDS with 5Y must be clamped to <= 3601 candles, found: " + seconds5Y.size());
        assertTrue(duration < 5000, "SECONDS with 5Y must execute quickly, took " + duration + "ms");

        // 2. MINUTE with 5Y range: must be clamped to at most 5000 candles and complete quickly
        start = System.currentTimeMillis();
        List<MarketDtos.CandleResponse> minute5Y = marketHistoryService.history("RELIANCE", "1m", "5Y", null, null);
        duration = System.currentTimeMillis() - start;

        assertNotNull(minute5Y);
        assertFalse(minute5Y.isEmpty());
        assertTrue(minute5Y.size() <= 5001, "MINUTE with 5Y must be clamped to <= 5001 candles, found: " + minute5Y.size());
        assertTrue(duration < 5000, "MINUTE with 5Y must execute quickly, took " + duration + "ms");

        // 3. SECONDS with 1D range: must be clamped to 1 hour (<= 3601)
        List<MarketDtos.CandleResponse> seconds1D = marketHistoryService.history("RELIANCE", "1s", "1D", null, null);
        assertNotNull(seconds1D);
        assertTrue(seconds1D.size() <= 3601, "SECONDS with 1D must be clamped to <= 3601 candles, found: " + seconds1D.size());
    }
}

