package in.shubhamprakash681.notification_service.services;

import in.shubhamprakash681.common_lib.security.JwtPrincipal;
import in.shubhamprakash681.notification_service.clients.MarketClient;
import in.shubhamprakash681.notification_service.dtos.StockResponse;
import in.shubhamprakash681.notification_service.dtos.WatchlistDtos;
import in.shubhamprakash681.notification_service.entity.WatchlistItem;
import in.shubhamprakash681.notification_service.repositories.WatchlistItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WatchlistService {
    private final WatchlistItemRepository watchlistItemRepository;
    private final WatchlistCacheService watchlistCacheService;
    private final MarketClient marketClient;

    @Transactional(readOnly = true)
    public List<WatchlistDtos.WatchlistResponse> watchlist(JwtPrincipal principal) {
        Long userId = principal.userId();
        return watchlistCacheService.get(userId)
                .orElseGet(() -> {
                    List<WatchlistDtos.WatchlistResponse> watchlist = watchlistItemRepository
                            .findByUserIdOrderBySymbolAsc(userId)
                            .stream()
                            .map(this::toResponse)
                            .toList();
                    watchlistCacheService.put(userId, watchlist);
                    return watchlist;
                });
    }

    @Transactional
    public WatchlistDtos.WatchlistResponse add(JwtPrincipal principal, WatchlistDtos.AddWatchlistRequest request) {
        Long userId = principal.userId();
        String symbol = normalizeSymbol(request.symbol());

        if (watchlistItemRepository.existsByUserIdAndSymbol(userId, symbol)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Symbol is already in watchlist");
        }

        StockResponse stock = marketClient.getStock(symbol);
        WatchlistItem item = watchlistItemRepository.save(WatchlistItem.builder()
                .userId(userId)
                .symbol(stock.symbol())
                .stockName(stock.name())
                .exchange(stock.exchange())
                .build());

        watchlistCacheService.evict(userId);
        return toResponse(item);
    }

    @Transactional
    public void remove(JwtPrincipal principal, String symbol) {
        Long userId = principal.userId();
        String normalized = normalizeSymbol(symbol);

        if (watchlistItemRepository.findByUserIdAndSymbol(userId, normalized).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Symbol is not in watchlist");
        }

        watchlistItemRepository.deleteByUserIdAndSymbol(userId, normalized);
        watchlistCacheService.evict(userId);
    }

    private WatchlistDtos.WatchlistResponse toResponse(WatchlistItem item) {
        return new WatchlistDtos.WatchlistResponse(
                item.getId(),
                item.getSymbol(),
                item.getStockName(),
                item.getExchange(),
                item.getCreatedAt());
    }

    private String normalizeSymbol(String symbol) {
        return symbol == null ? "" : symbol.trim().toUpperCase();
    }
}
