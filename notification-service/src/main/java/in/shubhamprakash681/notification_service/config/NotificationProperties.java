package in.shubhamprakash681.notification_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tradex.notifications")
@Data
public class NotificationProperties {
    private String priceTopic = "tradex.market.prices";
    private long watchlistCacheTtlMinutes = 10;
    private int notificationLimit = 100;
}
