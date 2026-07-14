package com.pomosda.permission.notification;

import com.pomosda.permission.common.CurrentUser;
import com.pomosda.permission.exception.ApiException;
import com.pomosda.permission.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository repository;
    private final CurrentUser currentUser;
    private final NotificationEmailService notificationEmailService;
    private final WhatsAppNotificationService whatsAppNotificationService;

    @Value("${app.notification.channel:EMAIL}")
    private String notificationChannel;

    @Transactional
    public void create(User user, String title, String message) {
        if (user == null) {
            return;
        }
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setChannel(channel());
        repository.save(notification);
        NotificationDeliveryResult result = send(user, title, message);
        notification.setDeliveryStatus(result.status());
        notification.setDeliveryError(result.error());
        if (result.status() == NotificationDeliveryStatus.SENT) {
            notification.setSentAt(Instant.now());
        }
    }

    private NotificationDeliveryResult send(User user, String title, String message) {
        if ("WHATSAPP".equals(channel())) {
            return whatsAppNotificationService.sendNotification(user, title, message);
        }
        return notificationEmailService.sendNotification(user, title, message);
    }

    private String channel() {
        if (notificationChannel == null || notificationChannel.isBlank()) {
            return "EMAIL";
        }
        return notificationChannel.trim().toUpperCase();
    }

    public List<NotificationDto> mine(Authentication authentication) {
        return repository.findByUserOrderByCreatedAtDesc(currentUser.get(authentication)).stream().map(NotificationDto::from).toList();
    }

    public NotificationDto read(UUID id, Authentication authentication) {
        Notification notification = repository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Notifikasi tidak ditemukan"));
        if (!notification.getUser().getId().equals(currentUser.get(authentication).getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Tidak dapat membaca notifikasi ini");
        }
        notification.setRead(true);
        return NotificationDto.from(repository.save(notification));
    }
}
