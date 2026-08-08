package in.shubhamprakash681.price_stream_service.controllers;

import in.shubhamprakash681.price_stream_service.dtos.PriceResponse;
import in.shubhamprakash681.price_stream_service.services.PriceQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/prices", "/api/prices"})
@RequiredArgsConstructor
public class PriceController {
    private final PriceQueryService priceQueryService;

    @GetMapping("/latest")
    List<PriceResponse> latest() {
        return priceQueryService.latest();
    }

    @GetMapping("/history")
    List<PriceResponse> history(@RequestParam(required = false) String symbol,
                                @RequestParam(defaultValue = "100") int limit) {
        return priceQueryService.history(symbol, limit);
    }

    @GetMapping("/{symbol}")
    PriceResponse latestBySymbol(@PathVariable String symbol) {
        return priceQueryService.latestBySymbol(symbol);
    }
}
