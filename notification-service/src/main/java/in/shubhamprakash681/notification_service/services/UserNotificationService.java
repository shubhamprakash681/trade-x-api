package in.shubhamprakash681.notification_service.services;

import in.shubhamprakash681.common_lib.security.JwtPrincipal;
import in.shubhamprakash681.notification_service.config.NotificationProperties;
import in.shubhamprakash681.notification_service.dtos.NotificationDtos;
import in.shubhamprakash681.notification_service.entity.UserNotification;
import in.shubhamprakash681.notification_service.repositories.UserNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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

    @Transactional(readOnly = true)
    public NotificationDtos.UnreadCountResponse unreadCount(JwtPrincipal principal) {
        return new NotificationDtos.UnreadCountResponse(
                userNotificationRepository.countByUserIdAndReadStatusFalse(principal.userId()));
    }

    @Transactional
    public NotificationDtos.NotificationResponse markAsRead(JwtPrincipal principal, Long notificationId) {
        UserNotification notification = userNotificationRepository.findByIdAndUserId(notificationId, principal.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));

        notification.setReadStatus(true);
        return toResponse(notification);
    }

    @Transactional
    public void markAllAsRead(JwtPrincipal principal) {
        userNotificationRepository.markAllAsRead(principal.userId());
    }

    private NotificationDtos.NotificationResponse toResponse(UserNotification notification) {
        return new NotificationDtos.NotificationResponse(
                notification.getId(),
                notification.getSymbol(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getAlertId(),
                notification.isReadStatus(),
                notification.getCreatedAt());
    }
}

