package in.shubhamprakash681.price_stream_service.data_seeding;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class SeedPriceCatalog {
    private final Map<String, BigDecimal> basePrice = Map.of(
            "NIFTYBEES", new BigDecimal("275.50"),
            "SNIFTYBEES", new BigDecimal("275.50"),
            "BANKBEES", new BigDecimal("515.25"),
            "HDFCBANK", new BigDecimal("1695.40"),
            "RELIANCE", new BigDecimal("2940.10"),
            "TCS", new BigDecimal("3890.70"),
            "INFY", new BigDecimal("1525.35"),
            "SBIN", new BigDecimal("835.80"),
            "ITC", new BigDecimal("432.15"),
            "SUNPHARMA", new BigDecimal("1510.20")
    );

    public BigDecimal basePrice(String symbol) {
        return basePrice.getOrDefault(symbol, new BigDecimal("100.00"));
    }

    public boolean isSynthetic(String symbol) {
        return symbol.startsWith("S");
    }
}
