package in.shubhamprakash681.price_stream_service.services;

import in.shubhamprakash681.price_stream_service.config.PriceStreamProperties;
import in.shubhamprakash681.price_stream_service.data_seeding.SeedPriceCatalog;
import in.shubhamprakash681.price_stream_service.dtos.PriceResponse;
import in.shubhamprakash681.price_stream_service.dtos.PriceTick;
import in.shubhamprakash681.price_stream_service.entity.PriceHistory;
import in.shubhamprakash681.price_stream_service.repositories.PriceHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PriceQueryService {
    private final PriceCacheService priceCacheService;
    private final PriceHistoryRepository priceHistoryRepository;
    private final PriceStreamProperties properties;
    private final SeedPriceCatalog seedPriceCatalog;

    @Transactional(readOnly = true)
    public List<PriceResponse> latest() {
        List<String> symbols = normalizedSymbols();
        List<PriceTick> cached = priceCacheService.all(symbols);

        if (!cached.isEmpty()) {
            return cached.stream().map(this::toResponse).toList();
        }

        /*
         * TODO: Generating random PriceTick here if it is not present in cache.
         *  Try to improve this logic later
         */
        return symbols.stream()
                .map(this::seedTick)
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PriceResponse latestBySymbol(String symbol) {
        String normalized = normalizeSymbol(symbol);

        return priceCacheService.get(normalized)
                .map(this::toResponse)
                .orElseGet(() -> latestHistory(normalized)
                        .orElseGet(() -> {
                            if (!normalizedSymbols().contains(normalized)) {
                                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Price not found");
                            }
                            return toResponse(seedTick(normalized));
                        })
                );
    }

    @Transactional(readOnly = true)
    public List<PriceResponse> history(String symbol, int limit) {
        int pageSize = Math.max(1, Math.min(limit, properties.getHistoryLimit()));

        if (symbol == null || symbol.isBlank()) {
            return priceHistoryRepository.findAllByOrderByTimestampDesc(PageRequest.of(0, pageSize)).stream()
                    .map(this::toResponse)
                    .toList();
        }

        String normalized = normalizeSymbol(symbol);
        return priceHistoryRepository.findBySymbolOrderByTimestampDesc(normalized, PageRequest.of(0, pageSize)).stream()
                .map(this::toResponse)
                .toList();
    }

    private Optional<PriceResponse> latestHistory(String symbol) {
        return priceHistoryRepository.findBySymbolOrderByTimestampDesc(symbol, PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .map(this::toResponse);
    }

    private PriceTick seedTick(String symbol) {
        BigDecimal basePrice = seedPriceCatalog.basePrice(symbol).setScale(4, RoundingMode.HALF_UP);

        return new PriceTick(
                symbol,
                basePrice,
                basePrice,
                BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP),
                BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP),
                seedPriceCatalog.isSynthetic(symbol),
                LocalDateTime.now());
    }

    private List<String> normalizedSymbols() {
        return properties.getSymbols().stream()
                .map(this::normalizeSymbol)
                .filter(symbol -> !symbol.isBlank())
                .distinct()
                .toList();
    }

    private PriceResponse toResponse(PriceTick tick) {
        return new PriceResponse(
                tick.symbol(),
                tick.price(),
                tick.previousPrice(),
                tick.changeAmount(),
                tick.changePercent(),
                tick.synthetic(),
                tick.timestamp());
    }

    private PriceResponse toResponse(PriceHistory history) {
        return new PriceResponse(
                history.getSymbol(),
                history.getPrice(),
                history.getPreviousPrice(),
                history.getChangeAmount(),
                history.getChangePercent(),
                history.isSynthetic(),
                history.getTimestamp());
    }

    private String normalizeSymbol(String symbol) {
        return symbol == null ? "" : symbol.trim().toUpperCase();
    }
}
