package in.shubhamprakash681.notification_service.repositories;

import in.shubhamprakash681.notification_service.entity.PriceAlert;
import in.shubhamprakash681.notification_service.enums.AlertStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface PriceAlertRepository extends JpaRepository<PriceAlert, Long> {
    List<PriceAlert> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<PriceAlert> findBySymbolAndStatus(String symbol, AlertStatus status);

    long deleteByUserIdAndId(Long userId, Long id);

    long deleteByUserIdAndSymbolIn(Long userId, Collection<String> symbols);
}
