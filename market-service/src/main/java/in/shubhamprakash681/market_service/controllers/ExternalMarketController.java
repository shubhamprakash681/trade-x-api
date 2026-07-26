package in.shubhamprakash681.market_service.controllers;

import in.shubhamprakash681.market_service.dtos.ExternalMarketDtos;
import in.shubhamprakash681.market_service.service.ExternalMarketService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/market")
@RequiredArgsConstructor
public class ExternalMarketController {
    private final ExternalMarketService externalMarketService;

    @GetMapping("/indices")
    List<ExternalMarketDtos.MarketIndexResponse> indices() {
        return externalMarketService.indices();
    }

    @GetMapping("/gainers")
    List<ExternalMarketDtos.MarketMoverResponse> gainers() {
        return externalMarketService.gainers();
    }

    @GetMapping("/losers")
    List<ExternalMarketDtos.MarketMoverResponse> losers() {
        return externalMarketService.losers();
    }

    @GetMapping("/trending")
    List<ExternalMarketDtos.MarketTrendResponse> trending() {
        return externalMarketService.trending();
    }
}
