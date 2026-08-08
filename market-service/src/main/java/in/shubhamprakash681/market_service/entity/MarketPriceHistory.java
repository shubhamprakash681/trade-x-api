package in.shubhamprakash681.market_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "market_price_candles",
        uniqueConstraints = @UniqueConstraint(name = "uk_market_price_candles_symbol_interval_time", columnNames = {"symbol", "candle_interval", "candle_time"}),
        indexes = {
                @Index(name = "idx_market_price_candles_symbol_interval_time", columnList = "symbol,candle_interval,candle_time"),
                @Index(name = "idx_market_price_candles_time", columnList = "candle_time")
        })
public class MarketPriceHistory {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(nullable = false, length = 32)
        private String symbol;

        @Column(name = "candle_interval", nullable = false, length = 16)
        private String interval;

        @Column(name = "candle_time", nullable = false)
        private LocalDateTime candleTime;

        @Column(nullable = false, precision = 18, scale = 4)
        private BigDecimal openPrice;

        @Column(nullable = false, precision = 18, scale = 4)
        private BigDecimal highPrice;

        @Column(nullable = false, precision = 18, scale = 4)
        private BigDecimal lowPrice;

        @Column(nullable = false, precision = 18, scale = 4)
        private BigDecimal closePrice;

        @Column(nullable = false)
        private Long volume;

        protected MarketPriceHistory() {
        }

        public MarketPriceHistory(String symbol,
                                  String interval,
                                  LocalDateTime candleTime,
                                  BigDecimal openPrice,
                                  BigDecimal highPrice,
                                  BigDecimal lowPrice,
                                  BigDecimal closePrice,
                                  Long volume) {
                this.symbol = symbol;
                this.interval = interval;
                this.candleTime = candleTime;
                this.openPrice = openPrice;
                this.highPrice = highPrice;
                this.lowPrice = lowPrice;
                this.closePrice = closePrice;
                this.volume = volume;
        }

        public Long getId() {
                return id;
        }

        public String getSymbol() {
                return symbol;
        }

        public String getInterval() {
                return interval;
        }

        public LocalDateTime getCandleTime() {
                return candleTime;
        }

        public BigDecimal getOpenPrice() {
                return openPrice;
        }

        public BigDecimal getHighPrice() {
                return highPrice;
        }

        public BigDecimal getLowPrice() {
                return lowPrice;
        }

        public BigDecimal getClosePrice() {
                return closePrice;
        }

        public Long getVolume() {
                return volume;
        }
}
