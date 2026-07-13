package in.shubhamprakash681.portfolio_service.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Entity
@Table(name = "portfolio_holdings", uniqueConstraints = {
        @UniqueConstraint(name = "uk_portfolio_holdings_user_symbol", columnNames = {"user_id", "symbol"})
})
@Data
@Builder
public class Holding {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 32)
    private String symbol;

    @Column(nullable = false, length = 180)
    private String stockName;

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal quantity;

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal averagePrice;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public void buy(BigDecimal buyQuantity, BigDecimal buyPrice, String latestStockName) {
        BigDecimal currentValue = quantity.multiply(averagePrice);
        BigDecimal buyValue = buyQuantity.multiply(buyPrice);
        BigDecimal newQuantity = quantity.add(buyQuantity);

        this.quantity = newQuantity;
        this.averagePrice = currentValue.add(buyValue).divide(newQuantity, 4, RoundingMode.HALF_UP);
        this.stockName = latestStockName;
    }

    public void sell(BigDecimal sellQuantity) {
        this.quantity = this.quantity.subtract(sellQuantity);
    }

    public boolean isEmpty() {
        return this.quantity.compareTo(BigDecimal.ZERO) == 0;
    }
}
