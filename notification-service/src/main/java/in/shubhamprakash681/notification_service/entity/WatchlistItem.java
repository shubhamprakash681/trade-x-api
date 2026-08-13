package in.shubhamprakash681.notification_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "watchlist_items", uniqueConstraints = {
        @UniqueConstraint(name = "uk_watchlist_items_user_symbol", columnNames = {"user_id", "symbol"})
})
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WatchlistItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 32)
    private String symbol;

    @Column(nullable = false, length = 180)
    private String stockName;

    @Column(nullable = false, length = 32)
    private String exchange;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;
}
