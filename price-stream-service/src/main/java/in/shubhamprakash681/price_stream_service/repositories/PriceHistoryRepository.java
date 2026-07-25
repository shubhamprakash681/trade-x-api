package in.shubhamprakash681.price_stream_service.repositories;

import in.shubhamprakash681.price_stream_service.entity.PriceHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {
    List<PriceHistory> findBySymbolOrderByTimestampDesc(String symbol, Pageable pageable);

    List<PriceHistory> findAllByOrderByTimestampDesc(Pageable pageable);
}
