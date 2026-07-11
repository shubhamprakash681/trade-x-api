package in.shubhamprakash681.market_service.controllers;

import in.shubhamprakash681.market_service.dtos.StockResponse;
import in.shubhamprakash681.market_service.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {
    private final StockService stockService;

    @GetMapping
    Page<StockResponse> stocks(@RequestParam(required = false) String q,
                               @PageableDefault(size = 20, sort = "symbol") Pageable pageable) {
        return stockService.findAll(q, pageable);
    }

    @GetMapping("/{symbol}")
    StockResponse stock(@PathVariable String symbol) {
        return stockService.findBySymbol(symbol);
    }

    @GetMapping("/search")
    List<StockResponse> search(@RequestParam String q) {
        return stockService.search(q);
    }

}
