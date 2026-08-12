package com.pomosda.permission.notification;

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

    private final RestTemplate restTemplate = new RestTemplate();

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