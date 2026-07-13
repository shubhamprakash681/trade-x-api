package in.shubhamprakash681.portfolio_service.dtos;

import in.shubhamprakash681.portfolio_service.enums.OrderSide;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        Long id,
        Long orderId,
        OrderSide type,
        BigDecimal amount,
        String description,
        LocalDateTime createdAt
) {
}
