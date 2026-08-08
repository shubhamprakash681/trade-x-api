package in.shubhamprakash681.market_service.config;

import in.shubhamprakash681.market_service.catalog.SupportedStockCatalog;
import in.shubhamprakash681.market_service.entity.Stock;
import in.shubhamprakash681.market_service.repositories.StockRepository;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class StockDataSeeder {
    @Bean
    SmartInitializingSingleton seedStocks(StockRepository stockRepository, SupportedStockCatalog supportedStockCatalog) {
        return () -> {
            List<Stock> supportedStocks = supportedStockCatalog.stocks().stream()
                    .map(SupportedStockCatalog.StockSeed::toEntity)
                    .toList();
            stockRepository.saveAll(supportedStocks);
        };
    }
}
