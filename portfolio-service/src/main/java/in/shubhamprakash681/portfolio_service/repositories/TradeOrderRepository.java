package in.shubhamprakash681.portfolio_service.repositories;

import in.shubhamprakash681.portfolio_service.entity.TradeOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TradeOrderRepository extends JpaRepository<TradeOrder, Long> {
    List<TradeOrder> findByUserIdOrderByCreatedAtDesc(Long userId);
}
