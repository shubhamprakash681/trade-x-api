package in.shubhamprakash681.market_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "price_history")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LivePriceTick {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 32)
    private String symbol;

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal price;

    @Column(nullable = false)
    private LocalDateTime timestamp;
}

