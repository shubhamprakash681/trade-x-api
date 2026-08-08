package in.shubhamprakash681.price_stream_service.data_seeding;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

import static java.util.Map.entry;

@Component
public class SeedPriceCatalog {
    private final Map<String, BigDecimal> basePrice = Map.ofEntries(
            entry("RELIANCE", new BigDecimal("2940.10")),
            entry("TCS", new BigDecimal("3890.70")),
            entry("INFY", new BigDecimal("1525.35")),
            entry("HDFCBANK", new BigDecimal("1695.40")),
            entry("ICICIBANK", new BigDecimal("1120.25")),
            entry("SBIN", new BigDecimal("835.80")),
            entry("ITC", new BigDecimal("432.15")),
            entry("LT", new BigDecimal("3615.60")),
            entry("AXISBANK", new BigDecimal("1185.35")),
            entry("BHARTIARTL", new BigDecimal("1418.75")),
            entry("MARUTI", new BigDecimal("12750.40")),
            entry("TITAN", new BigDecimal("3520.15")),
            entry("ASIANPAINT", new BigDecimal("2935.25")),
            entry("NIFTYBEES", new BigDecimal("275.50")),
            entry("BANKBEES", new BigDecimal("515.25"))
    );

    public BigDecimal basePrice(String symbol) {
        return basePrice.getOrDefault(symbol, new BigDecimal("100.00"));
    }
}
