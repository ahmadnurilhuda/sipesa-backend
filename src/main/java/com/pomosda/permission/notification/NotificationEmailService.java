package com.pomosda.permission.notification;

import com.pomosda.permission.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendEmail(String to, String subject, String body) {
        String url = "https://sandbox.api.mailtrap.io/api/send/" + inboxId;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Api-Token", apiToken);

        // Menggabungkan prefix dengan subjek yang dikirim
        String finalSubject = subjectPrefix + subject;

        Map<String, Object> requestBody = Map.of(
                "from", Map.of("email", fromEmail, "name", "Sistem SIPESA"),
                "to", List.of(Map.of("email", to)),
                "subject", finalSubject,
                "text", body // Gunakan "html" alih-alih "text" jika konten pesanmu berupa HTML
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
        // Catatan: Jika sebelumnya kamu memiliki template HTML khusus,
        // kamu bisa mengembalikannya dari Git history kamu.
        // Ini adalah contoh implementasi standarnya:
        String to = user.getEmail();
        String subject = "Kode OTP Reset Password";
        String body = "Halo,\n\nBerikut adalah kode OTP untuk mereset password Anda: " + otp + "\n\nKode ini bersifat rahasia.";

        // Panggil fungsi sendEmail yang sudah pakai Mailtrap API
        this.sendEmail(to, subject, body);
    }

    // 2. Tambahkan kembali method sendNotification
    public void sendNotification(User user, String subject, String message) {
        String to = user.getEmail();

        // Panggil fungsi sendEmail yang sudah pakai Mailtrap API
        this.sendEmail(to, subject, message);
    }
}