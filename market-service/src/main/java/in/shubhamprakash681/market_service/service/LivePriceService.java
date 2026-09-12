package in.shubhamprakash681.market_service.service;

import in.shubhamprakash681.market_service.entity.LivePriceTick;
import in.shubhamprakash681.market_service.repositories.LivePriceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LivePriceService {
    private final LivePriceRepository livePriceRepository;

    @Transactional(readOnly = true)
    public BigDecimal getLivePrice(String symbol, BigDecimal fallbackPrice) {
        if (symbol == null || symbol.isBlank()) {
            return fallbackPrice;
        }

        try {
            Optional<LivePriceTick> tick = livePriceRepository.findFirstBySymbolOrderByIdDesc(symbol.trim().toUpperCase());
            if (tick.isPresent() && tick.get().getPrice() != null && tick.get().getPrice().compareTo(BigDecimal.ZERO) > 0) {
                return tick.get().getPrice().setScale(4, RoundingMode.HALF_UP);
            }
        } catch (Exception e) {
            log.debug("Could not fetch live price for symbol {}: {}", symbol, e.getMessage());
        }

        return fallbackPrice != null ? fallbackPrice.setScale(4, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }
}

