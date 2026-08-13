package in.shubhamprakash681.notification_service.dtos;

import java.time.LocalDateTime;

public class NotificationDtos {
    public record NotificationResponse(
            Long id,
            String symbol,
            String title,
            String message,
            Long alertId,
            LocalDateTime createdAt
    ) {
    }
}
