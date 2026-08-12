package com.pomosda.permission.notification;

import com.pomosda.permission.common.CurrentUser;
import com.pomosda.permission.exception.ApiException;
import com.pomosda.permission.permission.PermissionRequest;
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

    /**
     * Mengirim notifikasi izin dengan template khusus yang lebih informatif
     */
    @Transactional
    public void sendPermissionNotification(User user, PermissionRequest permission, String approverName, String rejectionReason) {
        if (user == null) {
            return;
        }

        // Buat record notifikasi umum
        String title = getPermissionNotificationTitle(permission);
        String message = buildPermissionNotificationMessage(permission, approverName, rejectionReason);

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setChannel("EMAIL");
        repository.save(notification);

        // Kirim email dengan template khusus
        NotificationDeliveryResult result = notificationEmailService.sendPermissionEmail(user, permission, approverName, rejectionReason);
        notification.setDeliveryStatus(result.status());
        notification.setDeliveryError(result.error());
        if (result.status() == NotificationDeliveryStatus.SENT) {
            notification.setSentAt(Instant.now());
        }
    }

    private String getPermissionNotificationTitle(PermissionRequest permission) {
        return switch (permission.getStatus()) {
            case PENDING_WALI_KELAS -> "Pengajuan Izin Baru Diterima";
            case PENDING_WALI_KAMAR -> "Izin Menunggu Persetujuan Wali Kamar";
            case APPROVED -> "🎉 Izin Anda Telah Disetujui";
            case REJECTED_BY_WALI_KELAS -> "Izin Ditolak oleh Wali Kelas";
            case REJECTED_BY_WALI_KAMAR -> "Izin Ditolak oleh Wali Kamar";
            case COMPLETED -> "✅ Izin Telah Selesai";
            default -> "Notifikasi Izin";
        };
    }

    private String buildPermissionNotificationMessage(PermissionRequest permission, String approverName, String rejectionReason) {
        StringBuilder builder = new StringBuilder();
        builder.append("Izin ").append(permission.getPermissionType()).append("\n");
        builder.append("Santri: ").append(permission.getStudent().getName()).append("\n");
        builder.append("Status: ").append(permission.getStatus()).append("\n");
        if (approverName != null) {
            builder.append("Diproses oleh: ").append(approverName).append("\n");
        }
        if (rejectionReason != null && !rejectionReason.isBlank()) {
            builder.append("Alasan: ").append(rejectionReason);
        }
        return builder.toString();
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
