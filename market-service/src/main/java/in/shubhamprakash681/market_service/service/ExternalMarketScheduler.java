package in.shubhamprakash681.market_service.service;

import in.shubhamprakash681.market_service.config.ExternalMarketProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExternalMarketScheduler {
    private final ExternalMarketService externalMarketService;
    private final ExternalMarketProperties properties;

    @Scheduled(fixedDelayString = "${tradex.market.integration.refresh-interval-ms:30000}")
    public void refreshExternalSnapshot() {
        if (properties.isEnabled()) {
            externalMarketService.snapshotAndPublish();
        }
    }
}
