package in.shubhamprakash681.market_service.config;

import in.shubhamprakash681.market_service.entity.Stock;
import in.shubhamprakash681.market_service.repositories.StockRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

@Configuration
public class StockDataSeeder {
    @Bean
    ApplicationRunner seedStocks(StockRepository stockRepository) {
        return (ApplicationArguments args) -> {
            if (stockRepository.count() > 0) {
                return;
            }
            stockRepository.saveAll(List.of(
                    stock("NIFTYBEES", "Nippon India ETF Nifty 50 BeES", "NSE", "ETF", "275.50", false),
                    stock("SNIFTYBEES", "Synthetic Nifty 50 BeES", "TRADEX", "Synthetic ETF", "275.50", true),
                    stock("BANKBEES", "Nippon India ETF Bank BeES", "NSE", "ETF", "515.25", false),
                    stock("HDFCBANK", "HDFC Bank Limited", "NSE", "Banking", "1695.40", false),
                    stock("RELIANCE", "Reliance Industries Limited", "NSE", "Energy", "2940.10", false),
                    stock("TCS", "Tata Consultancy Services Limited", "NSE", "Technology", "3890.70", false),
                    stock("INFY", "Infosys Limited", "NSE", "Technology", "1525.35", false),
                    stock("SBIN", "State Bank of India", "NSE", "Banking", "835.80", false),
                    stock("ITC", "ITC Limited", "NSE", "Consumer Goods", "432.15", false),
                    stock("SUNPHARMA", "Sun Pharmaceutical Industries Limited", "NSE", "Pharmaceuticals", "1510.20", false)
            ));
        };
    }

    private Stock stock(String symbol, String name, String exchange, String sector, String price, boolean synthetic) {
        return Stock.builder()
                .symbol(symbol)
                .name(name)
                .exchange(exchange)
                .sector(sector)
                .referencePrice(new BigDecimal(price))
                .synthetic(synthetic)
                .build();
    }
}
