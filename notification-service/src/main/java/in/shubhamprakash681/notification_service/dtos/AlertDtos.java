package in.shubhamprakash681.notification_service.dtos;

import in.shubhamprakash681.notification_service.enums.AlertCondition;
import in.shubhamprakash681.notification_service.enums.AlertStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AlertDtos {
    public record CreateAlertRequest(
            @NotBlank @Size(max = 32) String symbol,
            @NotNull @DecimalMin(value = "0.0001") BigDecimal targetPrice,
            @NotNull AlertCondition condition
    ) {
    }

    public record AlertResponse(
            Long id,
            String symbol,
            String stockName,
            BigDecimal targetPrice,
            AlertCondition condition,
            AlertStatus status,
            BigDecimal triggeredPrice,
            LocalDateTime triggeredAt,
            LocalDateTime createdAt
    ) {
    }
}
