package in.shubhamprakash681.market_service.service.clients;

import in.shubhamprakash681.market_service.config.ExternalMarketProperties;
import in.shubhamprakash681.market_service.dtos.ExternalMarketDtos;
import in.shubhamprakash681.market_service.service.service_interfaces.ExternalMarketProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ThirdPartyMarketClient implements ExternalMarketProvider {
    private final ExternalMarketProperties properties;
    private final RestClient.Builder restClientBuilder;

    @Override
    public List<ExternalMarketDtos.MarketIndexResponse> indices() {
        return fetch("/indices", ExternalMarketDtos.MarketIndexResponse[].class);
    }

    @Override
    public List<ExternalMarketDtos.MarketMoverResponse> gainers() {
        return fetch("/gainers", ExternalMarketDtos.MarketMoverResponse[].class);
    }

    @Override
    public List<ExternalMarketDtos.MarketMoverResponse> losers() {
        return fetch("/losers", ExternalMarketDtos.MarketMoverResponse[].class);
    }

    @Override
    public List<ExternalMarketDtos.MarketTrendResponse> trending() {
        return fetch("/trending", ExternalMarketDtos.MarketTrendResponse[].class);
    }

    public boolean isAvailable() {
        return properties.isEnabled() && properties.getBaseUrl() != null && !properties.getBaseUrl().isBlank();
    }

    private <T> List<T> fetch(String path, Class<T[]> responseType) {
        if (!isAvailable()) {
            throw new IllegalStateException("External market provider is disabled");
        }

        RestClient restClient = restClientBuilder.baseUrl(properties.getBaseUrl()).build();
        T[] body = restClient.get()
                .uri(path)
                .headers(headers -> applyAuth(headers, properties.getApiKey()))
                .retrieve()
                .body(responseType);

        return body == null ? List.of() : List.of(body);
    }

    private void applyAuth(HttpHeaders headers, String apiKey) {
        if (apiKey != null && !apiKey.isBlank()) {
            headers.set("X-API-Key", apiKey);
        }
    }
}
