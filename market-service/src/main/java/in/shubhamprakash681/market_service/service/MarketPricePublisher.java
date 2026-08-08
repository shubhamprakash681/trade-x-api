package in.shubhamprakash681.market_service.service;

import in.shubhamprakash681.common_lib.stock.Helper;
import in.shubhamprakash681.market_service.dtos.MarketDtos;
import in.shubhamprakash681.market_service.dtos.PriceTick;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
public class MarketPricePublisher {
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private final KafkaTemplate<String, PriceTick> kafkaTemplate;
    private final String topic;

    public MarketPricePublisher(KafkaTemplate<String, PriceTick> kafkaTemplate,
                                @Value("${tradex.market.topic:tradex.market.prices}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void publishMover(MarketDtos.MarketMoverResponse moverResponse) {
        publish(moverResponse.symbol(), moverResponse.price(), moverResponse.changeAmount(),
                moverResponse.changePercent(), moverResponse.asOf(), Helper.isSymbolSyntheticStock(moverResponse.symbol()));
    }

    public void publishTrend(MarketDtos.MarketTrendResponse trendResponse) {
        BigDecimal changeAmount = trendResponse.price()
                .multiply(trendResponse.changePercent())
                .divide(ONE_HUNDRED, 4, RoundingMode.HALF_UP);

        publish(trendResponse.symbol(), trendResponse.price(), changeAmount,
                trendResponse.changePercent(), trendResponse.asOf(),
                Helper.isSymbolSyntheticStock(trendResponse.symbol()));
    }

    private void publish(String symbol,
                         BigDecimal price,
                         BigDecimal changeAmount,
                         BigDecimal changePercent,
                         LocalDateTime asOf,
                         boolean synthetic) {
        BigDecimal normalizedPrice = scale(price);
        BigDecimal normalizedChange = scale(changeAmount);
        BigDecimal previousPrice = normalizedPrice.subtract(normalizedChange).max(BigDecimal.ONE).setScale(4, RoundingMode.HALF_UP);

        PriceTick tick = new PriceTick(symbol, normalizedPrice, previousPrice, normalizedChange,
                changePercent.setScale(4, RoundingMode.HALF_UP),
                synthetic,
                asOf);

        try {
            kafkaTemplate.send(topic, symbol, tick);
        } catch (RuntimeException ignored) {
            // Market APIs should remain responsive even if Kafka is temporarily unavailable.
        }
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(4, RoundingMode.HALF_UP);
    }
}
