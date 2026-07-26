package in.shubhamprakash681.market_service.service.service_interfaces;

import in.shubhamprakash681.market_service.dtos.ExternalMarketDtos;

import java.util.List;

public interface ExternalMarketProvider {
    List<ExternalMarketDtos.MarketIndexResponse> indices();

    List<ExternalMarketDtos.MarketMoverResponse> gainers();

    List<ExternalMarketDtos.MarketMoverResponse> losers();

    List<ExternalMarketDtos.MarketTrendResponse> trending();
}
