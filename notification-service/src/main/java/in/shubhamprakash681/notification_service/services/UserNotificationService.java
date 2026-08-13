package in.shubhamprakash681.notification_service.services;

import in.shubhamprakash681.common_lib.security.JwtPrincipal;
import in.shubhamprakash681.notification_service.config.NotificationProperties;
import in.shubhamprakash681.notification_service.dtos.NotificationDtos;
import in.shubhamprakash681.notification_service.entity.UserNotification;
import in.shubhamprakash681.notification_service.repositories.UserNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserNotificationService {
    private final UserNotificationRepository userNotificationRepository;
    private final NotificationProperties properties;

    @Transactional(readOnly = true)
    public List<NotificationDtos.NotificationResponse> notifications(JwtPrincipal principal, int limit) {
        int pageSize = Math.max(1, Math.min(limit, properties.getNotificationLimit()));

        return userNotificationRepository.findByUserIdOrderByCreatedAtDesc(principal.userId(), PageRequest.of(0, pageSize))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private NotificationDtos.NotificationResponse toResponse(UserNotification notification) {
        return new NotificationDtos.NotificationResponse(
                notification.getId(),
                notification.getSymbol(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getAlertId(),
                notification.getCreatedAt());
    }
}
