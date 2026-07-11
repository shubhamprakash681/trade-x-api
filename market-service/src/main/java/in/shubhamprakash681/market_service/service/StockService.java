package in.shubhamprakash681.market_service.service;

import in.shubhamprakash681.market_service.dtos.StockResponse;
import in.shubhamprakash681.market_service.entity.Stock;
import in.shubhamprakash681.market_service.repositories.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockService {
    private final StockRepository stockRepository;

    @Transactional(readOnly = true)
    public Page<StockResponse> findAll(String q, Pageable pageable) {
        Page<Stock> stocks = q == null || q.isBlank()
                ? stockRepository.findAll(pageable)
                : stockRepository.findBySymbolContainingIgnoreCaseOrNameContainingIgnoreCase(q.trim(), q.trim(), pageable);

        return stocks.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public StockResponse findBySymbol(String symbol) {
        return stockRepository.findById(symbol.trim().toUpperCase())
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Stock not found"));
    }

    @Transactional(readOnly = true)
    public List<StockResponse> search(String q) {
        if (q == null || q.isEmpty()) return List.of();

        return stockRepository.findTop10BySymbolContainingIgnoreCaseOrNameContainingIgnoreCaseOrderBySymbolAsc(q.trim(), q.trim())
                .stream().map(this::toResponse)
                .toList();
    }

    StockResponse toResponse(Stock stock) {
        return new StockResponse(stock.getSymbol(),
                stock.getName(),
                stock.getExchange(),
                stock.getSector(),
                stock.getReferencePrice(),
                stock.isSynthetic());
    }
}
