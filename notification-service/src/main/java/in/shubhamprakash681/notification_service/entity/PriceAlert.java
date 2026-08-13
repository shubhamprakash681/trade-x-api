package in.shubhamprakash681.notification_service.entity;

import in.shubhamprakash681.notification_service.enums.AlertCondition;
import in.shubhamprakash681.notification_service.enums.AlertStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "price_alert", indexes = {
        @Index(name = "idx_price_alerts_symbol_status", columnList = "symbol,status"),
        @Index(name = "idx_price_alerts_user_status", columnList = "user_id,status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceAlert {
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
    private BigDecimal targetPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "alert_condition", nullable = false, length = 16)
    private AlertCondition condition;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, length = 16)
    private AlertStatus status = AlertStatus.ACTIVE;

    @Column(precision = 18, scale = 4)
    private BigDecimal triggeredPrice;

    private LocalDateTime triggeredAt;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public boolean isTriggeredBy(BigDecimal price) {
        return switch (condition) {
            case ABOVE -> price.compareTo(targetPrice) >= 0;
            case BELOW -> price.compareTo(targetPrice) <= 0;
        };
    }

    public void markTriggered(BigDecimal price, LocalDateTime timestamp) {
        this.status = AlertStatus.TRIGGERED;
        this.triggeredPrice = price;
        this.triggeredAt = timestamp;
    }
}
