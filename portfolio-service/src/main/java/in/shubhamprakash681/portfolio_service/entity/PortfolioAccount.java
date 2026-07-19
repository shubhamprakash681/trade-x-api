package in.shubhamprakash681.portfolio_service.entity;

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
@Table(name = "portfolio_accounts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @Column(nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal cashBalance;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public void debit(BigDecimal amount) {
        this.cashBalance = this.cashBalance.subtract(amount);
    }

    public void credit(BigDecimal amount) {
        this.cashBalance = this.cashBalance.add(amount);
    }
}
