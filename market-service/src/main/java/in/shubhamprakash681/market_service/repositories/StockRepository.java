package in.shubhamprakash681.market_service.repositories;

import in.shubhamprakash681.market_service.entity.Stock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockRepository extends JpaRepository<Stock, String> {
    Page<Stock> findBySymbolContainingIgnoreCaseOrNameContainingIgnoreCase(String symbol, String name, Pageable pageable);

    List<Stock> findTop10BySymbolContainingIgnoreCaseOrNameContainingIgnoreCaseOrderBySymbolAsc(String symbol, String name);
}
