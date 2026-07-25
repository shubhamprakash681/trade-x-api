package in.shubhamprakash681.price_stream_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "tradex.prices")
@Data
public class PriceStreamProperties {
    private String topic = "tradex.market.prices";
    private long generationIntervalMs = 2000;
    private int historyLimit = 500;
    private List<String> symbols = List.of(
            "NIFTYBEES",
            "SNIFTYBEES",
            "BANKBEES",
            "HDFCBANK",
            "RELIANCE",
            "TCS",
            "INFY",
            "SBIN",
            "ITC",
            "SUNPHARMA");
}
