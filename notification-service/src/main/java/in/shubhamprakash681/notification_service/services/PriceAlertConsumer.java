package in.shubhamprakash681.notification_service.services;

import in.shubhamprakash681.notification_service.dtos.PriceTick;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PriceAlertConsumer {
    private final AlertService alertService;

    @KafkaListener(topics = "${tradex.notifications.price-topic:tradex.market.prices}", groupId = "notification-service")
    public void consume(PriceTick priceTick) {
        alertService.evaluate(priceTick);
    }
}
