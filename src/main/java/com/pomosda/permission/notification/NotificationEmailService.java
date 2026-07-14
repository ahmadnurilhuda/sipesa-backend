package com.pomosda.permission.notification;

import com.pomosda.permission.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationEmailService {
    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${app.notification.email.enabled:false}")
    private boolean enabled;

    @Value("${app.notification.email.from:no-reply@sipesa.local}")
    private String from;

    @Value("${app.notification.email.subject-prefix:[SIPESA]}")
    private String subjectPrefix;

    public NotificationDeliveryResult sendNotification(User recipient, String title, String message) {
        if (recipient == null || recipient.getEmail() == null || recipient.getEmail().isBlank()) {
            return NotificationDeliveryResult.noEmail();
        }
        if (!enabled) {
            log.info("Email notification disabled. Pesan untuk {}: {} - {}", recipient.getEmail(), title, message);
            return NotificationDeliveryResult.disabled("Email");
        }

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.warn("Email notification is enabled, but JavaMailSender is not configured. Skipping email to {}.", recipient.getEmail());
            return NotificationDeliveryResult.failed("JavaMailSender belum dikonfigurasi");
        }

        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setFrom(from);
            mail.setTo(recipient.getEmail());
            mail.setSubject(subjectPrefix + " " + title);
            mail.setText(message);
            mailSender.send(mail);
            return NotificationDeliveryResult.sent();
        } catch (RuntimeException exception) {
            log.warn("Failed to send notification email to {}: {}", recipient.getEmail(), exception.getMessage());
            return NotificationDeliveryResult.failed(exception.getMessage());
        }
    }

    public NotificationDeliveryResult sendOtp(User recipient, String code) {
        return sendNotification(recipient, "Reset Password", """
                SIPESA SMP POMOSDA
                Reset Password

                Kode OTP Anda: %s

                Kode ini berlaku selama beberapa menit. Jangan berikan kode ini kepada siapa pun.

                Abaikan email ini jika Anda tidak meminta reset password.
                """.formatted(code));
    }
}
