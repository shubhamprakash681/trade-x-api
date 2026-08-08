package in.shubhamprakash681.market_service.catalog;

import in.shubhamprakash681.market_service.entity.Stock;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Component
public class SupportedStockCatalog {
    private final List<StockSeed> stocks = List.of(
            stock("RELIANCE", "Reliance Industries Limited", "NSE", "Energy", "2940.10"),
            stock("TCS", "Tata Consultancy Services Limited", "NSE", "Technology", "3890.70"),
            stock("INFY", "Infosys Limited", "NSE", "Technology", "1525.35"),
            stock("HDFCBANK", "HDFC Bank Limited", "NSE", "Banking", "1695.40"),
            stock("ICICIBANK", "ICICI Bank Limited", "NSE", "Banking", "1120.25"),
            stock("SBIN", "State Bank of India", "NSE", "Banking", "835.80"),
            stock("ITC", "ITC Limited", "NSE", "Consumer Goods", "432.15"),
            stock("LT", "Larsen & Toubro Limited", "NSE", "Infrastructure", "3615.60"),
            stock("AXISBANK", "Axis Bank Limited", "NSE", "Banking", "1185.35"),
            stock("BHARTIARTL", "Bharti Airtel Limited", "NSE", "Telecom", "1418.75"),
            stock("MARUTI", "Maruti Suzuki India Limited", "NSE", "Automobile", "12750.40"),
            stock("TITAN", "Titan Company Limited", "NSE", "Consumer Discretionary", "3520.15"),
            stock("ASIANPAINT", "Asian Paints Limited", "NSE", "Consumer Goods", "2935.25"),
            stock("NIFTYBEES", "Nippon India ETF Nifty 50 BeES", "NSE", "ETF", "275.50"),
            stock("BANKBEES", "Nippon India ETF Bank BeES", "NSE", "ETF", "515.25")
    );

    public List<StockSeed> stocks() {
        return stocks;
    }

    public List<String> symbols() {
        return stocks.stream()
                .map(StockSeed::symbol)
                .toList();
    }

    public Optional<StockSeed> findBySymbol(String symbol) {
        String normalized = symbol == null ? "" : symbol.trim().toUpperCase();
        return stocks.stream()
                .filter(stock -> stock.symbol().equals(normalized))
                .findFirst();
    }

    private StockSeed stock(String symbol, String name, String exchange, String sector, String referencePrice) {
        return new StockSeed(symbol, name, exchange, sector, new BigDecimal(referencePrice));
    }

    public record StockSeed(String symbol,
                            String name,
                            String exchange,
                            String sector,
                            BigDecimal referencePrice) {
        public Stock toEntity() {
            return new Stock(symbol, name, exchange, sector, referencePrice, false);
        }
    }
}
