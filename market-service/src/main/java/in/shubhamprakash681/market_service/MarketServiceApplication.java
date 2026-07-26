package in.shubhamprakash681.market_service;

import in.shubhamprakash681.common_lib.security.JwtProperties;
import in.shubhamprakash681.market_service.config.ExternalMarketProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({JwtProperties.class, ExternalMarketProperties.class})
public class MarketServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarketServiceApplication.class, args);
    }

}
