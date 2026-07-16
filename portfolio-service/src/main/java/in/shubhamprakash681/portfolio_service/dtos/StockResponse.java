package in.shubhamprakash681.portfolio_service.dtos;

import java.math.BigDecimal;

public record StockResponse(
        String symbol,
        String name,
        BigDecimal referencePrice
) {
}
