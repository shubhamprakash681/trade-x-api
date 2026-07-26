package in.shubhamprakash681.market_service.service;

import in.shubhamprakash681.market_service.dtos.ExternalMarketDtos;
import in.shubhamprakash681.market_service.service.service_interfaces.ExternalMarketProvider;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class FallbackExternalMarketProvider implements ExternalMarketProvider {
    @Override
    public List<ExternalMarketDtos.MarketIndexResponse> indices() {
        LocalDateTime now = LocalDateTime.now();
        return List.of(
                index("NIFTY50", "Nifty 50", "24684.85", "118.40", "0.48", now),
                index("BANKNIFTY", "Nifty Bank", "52315.20", "-84.55", "-0.16", now),
                index("SENSEX", "BSE Sensex", "80742.10", "332.15", "0.41", now));
    }

    @Override
    public List<ExternalMarketDtos.MarketMoverResponse> gainers() {
        LocalDateTime now = LocalDateTime.now();
        return List.of(
                mover("RELIANCE", "Reliance Industries Limited", "3012.45", "72.35", "2.46", 8452300L, now),
                mover("INFY", "Infosys Limited", "1562.80", "37.45", "2.45", 5319200L, now),
                mover("HDFCBANK", "HDFC Bank Limited", "1728.30", "32.90", "1.94", 6214400L, now));
    }

    @Override
    public List<ExternalMarketDtos.MarketMoverResponse> losers() {
        LocalDateTime now = LocalDateTime.now();
        return List.of(
                mover("TCS", "Tata Consultancy Services Limited", "3825.10", "-65.60", "-1.69", 2178800L, now),
                mover("SUNPHARMA", "Sun Pharmaceutical Industries Limited", "1484.95", "-25.25", "-1.67", 1987600L, now),
                mover("ITC", "ITC Limited", "425.65", "-6.50", "-1.50", 10442600L, now));
    }

    @Override
    public List<ExternalMarketDtos.MarketTrendResponse> trending() {
        LocalDateTime now = LocalDateTime.now();
        return List.of(
                trend("NIFTYBEES", "Nippon India ETF Nifty 50 BeES", "278.35", "1.03", "96.50", "High watchlist and index ETF activity", now),
                trend("SYNTH_NIFTYBEES", "Synthetic Nifty 50 BeES", "279.10", "1.31", "91.20", "Synthetic stream momentum", now),
                trend("SBIN", "State Bank of India", "842.85", "0.84", "86.75", "Banking sector participation", now));
    }

    private ExternalMarketDtos.MarketIndexResponse index(String symbol, String name, String value, String changeAmount, String changePercent, LocalDateTime asOf) {
        return new ExternalMarketDtos.MarketIndexResponse(symbol, name, bd(value), bd(changeAmount), bd(changePercent), asOf);
    }

    private ExternalMarketDtos.MarketMoverResponse mover(String symbol, String name, String price, String changeAmount, String changePercent, Long volume, LocalDateTime asOf) {
        return new ExternalMarketDtos.MarketMoverResponse(symbol, name, bd(price), bd(changeAmount), bd(changePercent), volume, asOf);
    }

    private ExternalMarketDtos.MarketTrendResponse trend(String symbol, String name, String price, String changePercent, String score, String reason, LocalDateTime asOf) {
        return new ExternalMarketDtos.MarketTrendResponse(symbol, name, bd(price), bd(changePercent), bd(score), reason, asOf);
    }

    private BigDecimal bd(String value) {
        return new BigDecimal(value);
    }
}
