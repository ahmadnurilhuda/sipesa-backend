package com.pomosda.permission.notification;

import com.pomosda.permission.permission.PermissionRequest;
import com.pomosda.permission.permission.PermissionStatus;
import com.pomosda.permission.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationEmailService {

    @Value("${mailtrap.api.token}")
    private String apiToken;

    @Value("${mailtrap.api.inbox-id}")
    private String inboxId;

    // Membaca email pengirim dari application.yml
    @Value("${app.notification.email.from}")
    private String fromEmail;

    // Membaca prefix subjek dari application.yml
    @Value("${app.notification.email.subject-prefix}")
    private String subjectPrefix;

    @Value("${app.notification.app-url:http://localhost:3000}")
    private String appUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final ZoneId JAKARTA_ZONE = ZoneId.of("Asia/Jakarta");

    public void sendEmail(String to, String subject, String body) {
        String url = "https://sandbox.api.mailtrap.io/api/send/" + inboxId;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Api-Token", apiToken);

        // Menggabungkan prefix dengan subjek yang dikirim
        String finalSubject = subjectPrefix + subject;

        // Kirim kedua versi: text (plain) dan html (jika body sudah HTML)
        String html = body == null ? "" : body;
        String text = stripHtmlTags(html);

        Map<String, Object> requestBody = Map.of(
                "from", Map.of("email", fromEmail, "name", "SIPESA SMP POMOSDA"),
                "to", List.of(Map.of("email", to)),
                "subject", finalSubject,
                "text", text,
                "html", html
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            System.out.println("Email berhasil dikirim via Mailtrap API. Status: " + response.getStatusCode());
        } catch (Exception e) {
            System.err.println("Gagal mengirim email via API: " + e.getMessage());
        }
    }

    public void sendOtp(User user, String otp) {
        String to = user.getEmail();
        String subject = "Kode OTP Reset Password";

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("name", user.getName() == null ? "" : user.getName());
        placeholders.put("otp", otp);
        placeholders.put("expiry", "10");

        String html = renderTemplate("otp.html", placeholders);

        this.sendEmail(to, subject, html);
    }

    // 2. Tambahkan kembali method sendNotification
    public NotificationDeliveryResult sendNotification(User user, String subject, String message) {
        String to = user.getEmail();
        try {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("name", user.getName() == null ? "" : user.getName());
            placeholders.put("message", message);
            placeholders.put("subject", subject);

            String html = renderTemplate("generic.html", placeholders);

            this.sendEmail(to, subject, html);

            return new NotificationDeliveryResult(
                    NotificationDeliveryStatus.SENT,
                    "Pesan berhasil dikirim ke Mailtrap"
            );

        } catch (Exception e) {

            return new NotificationDeliveryResult(
                    NotificationDeliveryStatus.FAILED,
                    "Gagal mengirim pesan: " + e.getMessage()
            );

        }
    }

    /**
     * Mengirim email notifikasi izin dengan template khusus berdasarkan status
     */
    public NotificationDeliveryResult sendPermissionEmail(User user, PermissionRequest permission, String approverName, String rejectionReason) {
        try {
            String to = user.getEmail();
            String templateName = getPermissionTemplateByStatus(permission.getStatus());
            String subject = getPermissionSubjectByStatus(permission.getStatus());
            
            Map<String, String> placeholders = buildPermissionEmailPlaceholders(permission, approverName, rejectionReason, user.getName());
            
            String html = renderTemplate(templateName, placeholders);
            this.sendEmail(to, subject, html);
            
            return new NotificationDeliveryResult(
                    NotificationDeliveryStatus.SENT,
                    "Email izin berhasil dikirim"
            );
        } catch (Exception e) {
            return new NotificationDeliveryResult(
                    NotificationDeliveryStatus.FAILED,
                    "Gagal mengirim email izin: " + e.getMessage()
            );
        }
    }

    private String getPermissionTemplateByStatus(PermissionStatus status) {
        return switch (status) {
            case PENDING_WALI_KELAS -> "permission-submitted.html";
            case PENDING_WALI_KAMAR -> "permission-pending-approval.html";
            case APPROVED -> "permission-approved.html";
            case REJECTED_BY_WALI_KELAS, REJECTED_BY_WALI_KAMAR -> "permission-rejected.html";
            case COMPLETED -> "permission-completed.html";
            default -> "generic.html";
        };
    }

    private String getPermissionSubjectByStatus(PermissionStatus status) {
        return switch (status) {
            case PENDING_WALI_KELAS -> "Pengajuan Izin Baru Diterima";
            case PENDING_WALI_KAMAR -> "Izin Menunggu Persetujuan Wali Kamar";
            case APPROVED -> "🎉 Izin Anda Telah Disetujui";
            case REJECTED_BY_WALI_KELAS -> "❌ Izin Ditolak oleh Wali Kelas";
            case REJECTED_BY_WALI_KAMAR -> "❌ Izin Ditolak oleh Wali Kamar";
            case COMPLETED -> "✅ Izin Telah Selesai";
            default -> "Notifikasi Izin";
        };
    }

    private Map<String, String> buildPermissionEmailPlaceholders(PermissionRequest permission, String approverName, String rejectionReason, String studentName) {
        Map<String, String> placeholders = new HashMap<>();
        
        // Data dasar
        placeholders.put("studentName", studentName == null ? "" : studentName);
        placeholders.put("permissionId", permission.getId().toString());
        placeholders.put("permissionType", permission.getPermissionType() == null ? "" : permission.getPermissionType());
        placeholders.put("reason", permission.getReason() == null ? "" : permission.getReason());
        placeholders.put("destination", permission.getDestination() == null ? "" : permission.getDestination());
        
        // Format tanggal dan waktu
        String startDate = formatInstant(permission.getStartAt(), "dd MMMM yyyy");
        String startTime = formatInstant(permission.getStartAt(), "HH:mm");
        String expectedReturnDate = formatInstant(permission.getExpectedReturnAt(), "dd MMMM yyyy");
        String expectedReturnTime = formatInstant(permission.getExpectedReturnAt(), "HH:mm");
        
        placeholders.put("startDate", startDate);
        placeholders.put("startTime", startTime);
        placeholders.put("expectedReturnDate", expectedReturnDate);
        placeholders.put("expectedReturnTime", expectedReturnTime);
        
        // Durasi
        long durationDays = ChronoUnit.DAYS.between(
                permission.getStartAt().atZone(JAKARTA_ZONE).toLocalDate(),
                permission.getExpectedReturnAt().atZone(JAKARTA_ZONE).toLocalDate()
        ) + 1;
        placeholders.put("durationDays", String.valueOf(Math.max(1, durationDays)));
        
        // Info persetujuan
        placeholders.put("approverName", approverName == null ? "Wali Kelas" : approverName);
        placeholders.put("currentApprovalStage", getCurrentApprovalStage(permission.getStatus()));
        placeholders.put("currentApprovalStageDesc", getApprovalStageDescription(permission.getStatus()));
        
        // Info penolakan dan catatan
        placeholders.put("rejectionReason", rejectionReason == null ? "" : rejectionReason);
        placeholders.put("note", rejectionReason == null ? "" : rejectionReason);
        placeholders.put("rejectedBy", approverName == null ? "Wali Sekolah" : approverName);
        
        // Deskripsi tahap persetujuan (untuk template urutan persetujuan)
        placeholders.put("waliKelasDesc", "Tahap pertama - Persetujuan dari Wali Kelas");
        placeholders.put("waliKamarDesc", "Tahap kedua - Persetujuan dari Wali Kamar");
        placeholders.put("completedDesc", "Izin selesai setelah check-in kembali");
        
        // Waktu selesai
        if (permission.getCompletedAt() != null) {
            placeholders.put("completedAt", formatInstant(permission.getCompletedAt(), "dd MMMM yyyy HH:mm"));
        } else {
            placeholders.put("completedAt", "");
        }
        
        // Permission ID
        placeholders.put("permissionId", permission.getId().toString());
        
        // App URL
        placeholders.put("appUrl", appUrl);
        
        // Timestamp pengiriman
        placeholders.put("sentAt", formatInstant(Instant.now(), "dd MMMM yyyy HH:mm"));
        
        return placeholders;
    }

    private String getCurrentApprovalStage(PermissionStatus status) {
        return switch (status) {
            case PENDING_WALI_KELAS -> "Menunggu Persetujuan Wali Kelas";
            case PENDING_WALI_KAMAR -> "Menunggu Persetujuan Wali Kamar";
            case APPROVED -> "Disetujui - Menunggu Keberangkatan";
            case REJECTED_BY_WALI_KELAS, REJECTED_BY_WALI_KAMAR -> "Ditolak";
            case COMPLETED -> "Selesai";
            default -> "Dalam Proses";
        };
    }

    private String getApprovalStageDescription(PermissionStatus status) {
        return switch (status) {
            case PENDING_WALI_KELAS -> "Wali Kelas sedang meninjau pengajuan Anda. Anda akan menerima notifikasi setelah ada keputusan.";
            case PENDING_WALI_KAMAR -> "Wali Kelas telah menyetujui. Sekarang menunggu persetujuan dari Wali Kamar.";
            case APPROVED -> "Kedua Wali telah menyetujui izin Anda. QR Code sudah tersedia untuk digunakan.";
            case REJECTED_BY_WALI_KELAS, REJECTED_BY_WALI_KAMAR -> "Izin tidak dapat disetujui pada saat ini.";
            case COMPLETED -> "Izin telah selesai dan dikonfirmasi oleh semua pihak.";
            default -> "Izin sedang diproses.";
        };
    }

    private String formatInstant(Instant instant, String pattern) {
        if (instant == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern)
                .withZone(JAKARTA_ZONE);
        return formatter.format(instant);
    }

    private String loadTemplate(String templateName) throws IOException {
        ClassPathResource resource = new ClassPathResource("email_templates/" + templateName);
        try (InputStream is = resource.getInputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private String renderTemplate(String templateName, Map<String, String> placeholders) {
        try {
            String tpl = loadTemplate(templateName);
            for (Map.Entry<String, String> e : placeholders.entrySet()) {
                String key = "{{" + e.getKey() + "}}";
                String value = e.getValue() == null ? "" : e.getValue();
                tpl = tpl.replace(key, value);
            }
            // Remove any unreplaced placeholders to avoid showing raw template syntax
            tpl = tpl.replaceAll("\\{\\{[^}]*\\}\\}", "");
            return tpl;
        } catch (IOException ex) {
            // fallback ke versi plain text jika template tidak ditemukan
            return placeholders.getOrDefault("message", placeholders.getOrDefault("body", ""));
        }
    }

    private String stripHtmlTags(String html) {
        if (html == null) return "";
        return html.replaceAll("<[^>]*>", "").replaceAll("&nbsp;", " ");
    }
}