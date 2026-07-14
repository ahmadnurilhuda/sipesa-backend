package com.pomosda.permission.notification;

import java.time.Instant;
import java.util.UUID;

public record NotificationDto(
        UUID id,
        String title,
        String message,
        boolean read,
        String channel,
        NotificationDeliveryStatus deliveryStatus,
        String deliveryError,
        Instant sentAt,
        Instant createdAt
) {
    public static NotificationDto from(Notification notification) {
        return new NotificationDto(
                notification.getId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.isRead(),
                notification.getChannel(),
                notification.getDeliveryStatus(),
                notification.getDeliveryError(),
                notification.getSentAt(),
                notification.getCreatedAt()
        );
    }
}
