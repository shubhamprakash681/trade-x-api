package in.shubhamprakash681.price_stream_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "price_history")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class PriceHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @Column(nullable = false, length = 32)
    private String symbol;

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal price;

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal previousPrice;

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal changeAmount;

    @Column(nullable = false, precision = 9, scale = 4)
    private BigDecimal changePercent;

    @Column(nullable = false)
    private boolean synthetic;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime timestamp;
}
