package in.shubhamprakash681.market_service.controllers;

import in.shubhamprakash681.market_service.dtos.MarketDtos;
import in.shubhamprakash681.market_service.service.MarketHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class MarketController {
    private final MarketHistoryService marketHistoryService;

    @GetMapping({"/market/history/{symbol}", "/api/market/history/{symbol}"})
    List<MarketDtos.CandleResponse> history(
            @PathVariable String symbol,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return marketHistoryService.history(symbol, from, to);
    }

    @GetMapping({"/market/candle/{symbol}", "/api/market/candle/{symbol}"})
    MarketDtos.CandleResponse candle(@PathVariable String symbol) {
        return marketHistoryService.latestCandle(symbol);
    }

    @GetMapping({"/market/gainers", "/api/market/gainers"})
    List<MarketDtos.MarketMoverResponse> gainers() {
        return marketHistoryService.gainers();
    }

    @GetMapping({"/market/losers", "/api/market/losers"})
    List<MarketDtos.MarketMoverResponse> losers() {
        return marketHistoryService.losers();
    }

    @GetMapping({"/market/trending", "/api/market/trending"})
    List<MarketDtos.MarketTrendResponse> trending() {
        return marketHistoryService.trending();
    }

    @GetMapping({"/market/admin/status", "/api/admin/market/status"})
    MarketDtos.MarketStatusResponse status() {
        return marketHistoryService.status();
    }

    @PostMapping({"/market/admin/regenerate", "/api/admin/market/regenerate"})
    MarketDtos.MarketStatusResponse regenerate() {
        return marketHistoryService.regenerateMissingHistory();
    }
}
