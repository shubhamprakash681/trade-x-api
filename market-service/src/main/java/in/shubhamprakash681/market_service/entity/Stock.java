package in.shubhamprakash681.market_service.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Data
@Table(name = "stocks_table")
@Builder
@AllArgsConstructor
public class Stock {
    @Id
    @Column(nullable = false, unique = true, length = 32)
    private String symbol;

    @Column(nullable = false, length = 180)
    private String name;

    @Column(nullable = false, length = 32)
    private String exchange;

    @Column(nullable = false, length = 80)
    private String sector;

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal referencePrice;

    @Column(nullable = false)
    private boolean synthetic;

    protected Stock() {
    }

}
