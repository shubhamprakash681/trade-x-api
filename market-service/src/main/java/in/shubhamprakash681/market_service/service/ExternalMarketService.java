package in.shubhamprakash681.market_service.service;

import in.shubhamprakash681.market_service.config.ExternalMarketProperties;
import in.shubhamprakash681.market_service.dtos.ExternalMarketDtos;
import in.shubhamprakash681.market_service.service.clients.ThirdPartyMarketClient;
import in.shubhamprakash681.market_service.service.service_interfaces.ExternalMarketProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExternalMarketService {
    private final ThirdPartyMarketClient thirdPartyMarketClient;
    private final FallbackExternalMarketProvider fallbackExternalMarketProvider;
    private final MarketPricePublisher marketPricePublisher;
    private final ExternalMarketProperties properties;

    public List<ExternalMarketDtos.MarketIndexResponse> indices() {
        List<ExternalMarketDtos.MarketIndexResponse> responses = externalMarketProvider().indices();
        publishIndices(responses);

        return responses;
    }

    public List<ExternalMarketDtos.MarketMoverResponse> gainers() {
        List<ExternalMarketDtos.MarketMoverResponse> responses = externalMarketProvider().gainers();
        publishMovers(responses);

        return responses;
    }

    public List<ExternalMarketDtos.MarketMoverResponse> losers() {
        List<ExternalMarketDtos.MarketMoverResponse> responses = externalMarketProvider().losers();
        publishMovers(responses);

        return responses;
    }

    public List<ExternalMarketDtos.MarketTrendResponse> trending() {
        List<ExternalMarketDtos.MarketTrendResponse> responses = externalMarketProvider().trending();
        publishTrends(responses);

        return responses;
    }

    public void snapshotAndPublish() {
        ExternalMarketProvider selectedMarketProvider = externalMarketProvider();

        List<ExternalMarketDtos.MarketIndexResponse> indices = selectedMarketProvider.indices();
        List<ExternalMarketDtos.MarketMoverResponse> gainers = selectedMarketProvider.gainers();
        List<ExternalMarketDtos.MarketMoverResponse> losers = selectedMarketProvider.losers();
        List<ExternalMarketDtos.MarketTrendResponse> trending = selectedMarketProvider.trending();

        publishIndices(indices);
        publishMovers(gainers);
        publishMovers(losers);
        publishTrends(trending);

//        return new ExternalMarketDtos.MarketSnapshot(indices, gainers, losers, trending);
    }

    private void publishIndices(List<ExternalMarketDtos.MarketIndexResponse> indexResponses) {
        if (properties.isPublishOnRequest()) {
            indexResponses.forEach(marketPricePublisher::publishIndex);
        }
    }

    private void publishMovers(List<ExternalMarketDtos.MarketMoverResponse> moverResponse) {
        if (properties.isPublishOnRequest()) {
            moverResponse.forEach(marketPricePublisher::publishMover);
        }
    }

    private void publishTrends(List<ExternalMarketDtos.MarketTrendResponse> trendResponses) {
        if (properties.isPublishOnRequest()) {
            trendResponses.forEach(marketPricePublisher::publishTrend);
        }
    }

    private ExternalMarketProvider externalMarketProvider() {
        if (!thirdPartyMarketClient.isAvailable()) {
            return fallbackExternalMarketProvider;
        }

        return new SafeExternalMarketProvider(thirdPartyMarketClient, fallbackExternalMarketProvider);
    }

    private record SafeExternalMarketProvider(ExternalMarketProvider primary,
                                              ExternalMarketProvider fallback) implements ExternalMarketProvider {

        @Override
        public List<ExternalMarketDtos.MarketIndexResponse> indices() {
            try {
                return primary.indices();
            } catch (RuntimeException exception) {
                return fallback.indices();
            }
        }

        @Override
        public List<ExternalMarketDtos.MarketMoverResponse> gainers() {
            try {
                return primary.gainers();
            } catch (RuntimeException exception) {
                return fallback.gainers();
            }
        }

        @Override
        public List<ExternalMarketDtos.MarketMoverResponse> losers() {
            try {
                return primary.losers();
            } catch (RuntimeException exception) {
                return fallback.losers();
            }
        }

        @Override
        public List<ExternalMarketDtos.MarketTrendResponse> trending() {
            try {
                return primary.trending();
            } catch (RuntimeException exception) {
                return fallback.trending();
            }
        }
    }
}
