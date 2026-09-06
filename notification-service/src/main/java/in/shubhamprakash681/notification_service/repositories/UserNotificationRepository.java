package in.shubhamprakash681.notification_service.repositories;

import in.shubhamprakash681.notification_service.entity.UserNotification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {
    List<UserNotification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    long countByUserIdAndReadStatusFalse(Long userId);

    Optional<UserNotification> findByIdAndUserId(Long id, Long userId);

    @Modifying
    @Query("UPDATE UserNotification n SET n.readStatus = true WHERE n.userId = :userId AND n.readStatus = false")
    int markAllAsRead(Long userId);
}
