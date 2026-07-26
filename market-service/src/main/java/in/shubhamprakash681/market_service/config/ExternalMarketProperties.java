package in.shubhamprakash681.market_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tradex.market.integration")
@Data
public class ExternalMarketProperties {
    private boolean enabled;
    private String baseUrl;
    private String apiKey;
    private long refreshIntervalMs = 30000;
    private boolean publishOnRequest = true;
}
