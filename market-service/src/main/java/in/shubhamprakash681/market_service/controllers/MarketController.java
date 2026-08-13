package in.shubhamprakash681.market_service.controllers;

import in.shubhamprakash681.market_service.dtos.MarketDtos;
import in.shubhamprakash681.market_service.service.MarketHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class MarketController {
    private final MarketHistoryService marketHistoryService;

    @GetMapping("/api/market/history/{symbol}")
    List<MarketDtos.CandleResponse> history(
            @PathVariable String symbol,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return marketHistoryService.history(symbol, from, to);
    }

    @GetMapping("/api/market/candle/{symbol}")
    MarketDtos.CandleResponse candle(@PathVariable String symbol) {
        return marketHistoryService.latestCandle(symbol);
    }

    @GetMapping("/api/market/gainers")
    List<MarketDtos.MarketMoverResponse> gainers() {
        return marketHistoryService.gainers();
    }

    @GetMapping("/api/market/losers")
    List<MarketDtos.MarketMoverResponse> losers() {
        return marketHistoryService.losers();
    }

    @GetMapping("/api/market/trending")
    List<MarketDtos.MarketTrendResponse> trending() {
        return marketHistoryService.trending();
    }

    @GetMapping("/api/admin/market/status")
    MarketDtos.MarketStatusResponse status() {
        return marketHistoryService.status();
    }

    @PostMapping("/api/admin/market/regenerate")
    MarketDtos.MarketStatusResponse regenerate() {
        return marketHistoryService.regenerateMissingHistory();
    }
}
