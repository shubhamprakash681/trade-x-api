package in.shubhamprakash681.portfolio_service.dtos;

import in.shubhamprakash681.portfolio_service.enums.OrderSide;
import in.shubhamprakash681.portfolio_service.enums.OrderStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderDtos() {
    public record OrderRequest(
            @NotBlank @Size(max = 32) String symbol,
            @NotNull @DecimalMin(value = "0.0001") BigDecimal quantity
    ) {
    }

    public record OrderResponse(
            Long id,
            Long userId,
            String symbol,
            String stockName,
            OrderSide side,
            BigDecimal quantity,
            BigDecimal price,
            BigDecimal totalAmount,
            OrderStatus status,
            LocalDateTime createdAt
    ) {

    }
}
