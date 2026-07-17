package in.shubhamprakash681.portfolio_service.clients;

import in.shubhamprakash681.portfolio_service.dtos.StockResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "market-service")
public interface MarketClient {
    @GetMapping("/api/stocks/{symbol}")
    StockResponse getStock(@PathVariable("symbol") String symbol);
}
