package in.shubhamprakash681.notification_service.clients;

import in.shubhamprakash681.notification_service.dtos.MarketDtos;
import in.shubhamprakash681.notification_service.dtos.StockResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "market-service")
public interface MarketClient {
    @GetMapping("/api/stocks/{symbol}")
    StockResponse getStock(@PathVariable("symbol") String symbol);

    @GetMapping("/api/market/gainers")
    List<MarketDtos.MarketMoverResponse> gainers();

    @GetMapping("/api/market/losers")
    List<MarketDtos.MarketMoverResponse> losers();

    @GetMapping("/api/market/trending")
    List<MarketDtos.MarketTrendResponse> trending();
}
