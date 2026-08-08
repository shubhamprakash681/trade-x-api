package in.shubhamprakash681.market_service.repositories;

import in.shubhamprakash681.market_service.entity.MarketPriceHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface MarketPriceHistoryRepository extends JpaRepository<MarketPriceHistory, Long> {
    List<MarketPriceHistory> findBySymbolAndIntervalAndCandleTimeBetweenOrderByCandleTimeAsc(String symbol,
                                                                                             String interval,
                                                                                             LocalDateTime startTime,
                                                                                             LocalDateTime endTime);

    List<MarketPriceHistory> findBySymbolAndIntervalOrderByCandleTimeDesc(String symbol, String interval, Pageable pageable);

    Optional<MarketPriceHistory> findFirstBySymbolAndIntervalOrderByCandleTimeDesc(String symbol, String interval);

    long countBySymbolAndIntervalAndCandleTimeBetween(String symbol,
                                                      String interval,
                                                      LocalDateTime startTime,
                                                      LocalDateTime endTime);

    Optional<MarketPriceHistory> findFirstBySymbolAndIntervalOrderByCandleTimeAsc(String symbol, String interval);

    @Query("select h.candleTime from MarketPriceHistory h where h.symbol = :symbol and h.interval = :interval and h.candleTime between :startTime and :endTime")
    Set<LocalDateTime> findExistingTimes(@Param("symbol") String symbol,
                                         @Param("interval") String interval,
                                         @Param("startTime") LocalDateTime startTime,
                                         @Param("endTime") LocalDateTime endTime);

    @Query("""
            select h from MarketPriceHistory h
            where h.symbol in :symbols
            and h.interval = :interval
            and h.candleTime = (
                select max(innerHistory.candleTime)
                from MarketPriceHistory innerHistory
                where innerHistory.symbol = h.symbol
                and innerHistory.interval = :interval
            )
            """)
    List<MarketPriceHistory> findLatestForSymbols(@Param("symbols") Collection<String> symbols,
                                                  @Param("interval") String interval);
}
