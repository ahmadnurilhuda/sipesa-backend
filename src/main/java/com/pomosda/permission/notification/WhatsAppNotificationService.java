package com.pomosda.permission.notification;

import com.pomosda.permission.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Service
public class WhatsAppNotificationService {
    private final RestClient restClient;
    private final boolean enabled;
    private final String apiKey;

    public WhatsAppNotificationService(
            RestClient.Builder restClientBuilder,
            @Value("${app.whatsapp.enabled:false}") boolean enabled,
            @Value("${app.whatsapp.gateway-url:http://localhost:3010}") String gatewayUrl,
            @Value("${app.whatsapp.gateway-api-key:}") String apiKey
    ) {
        this.restClient = restClientBuilder.baseUrl(gatewayUrl).build();
        this.enabled = enabled;
        this.apiKey = apiKey;
    }

    public NotificationDeliveryResult sendNotification(User user, String title, String message) {
        if (user == null) {
            return NotificationDeliveryResult.noPhone();
        }
        return sendText(user.getPhone(), "SIPESA SMP POMOSDA\n" + title + "\n\n" + message);
    }

    public NotificationDeliveryResult sendOtp(String phone, String code) {
        return sendText(phone, """
                SIPESA SMP POMOSDA
                Reset Password

                Kode OTP Anda: %s

                Kode ini berlaku selama beberapa menit. Jangan berikan kode ini kepada siapa pun, termasuk pihak yang mengaku sebagai petugas.

                Abaikan pesan ini jika Anda tidak meminta reset password.
                """.formatted(code));
    }

    private NotificationDeliveryResult sendText(String rawPhone, String message) {
        String phone = normalizePhone(rawPhone);
        if (phone.isBlank()) {
            return NotificationDeliveryResult.noPhone();
        }
        if (!enabled) {
            log.info("WhatsApp gateway disabled. Pesan untuk {}: {}", phone, message);
            return NotificationDeliveryResult.disabled("WhatsApp API");
        }
        try {
            GatewaySendResponse response = restClient.post()
                    .uri("/send-message")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(headers -> {
                        if (!apiKey.isBlank()) {
                            headers.set("X-API-Key", apiKey);
                        }
                    })
                    .body(new GatewaySendRequest(phone, message))
                    .retrieve()
                    .body(GatewaySendResponse.class);
            if (response != null && response.success()) {
                return NotificationDeliveryResult.sent();
            }
            return NotificationDeliveryResult.failed(response == null ? "Gateway WhatsApp tidak mengembalikan response" : response.message());
        } catch (RestClientException exception) {
            log.warn("Gagal mengirim WhatsApp ke {} melalui gateway", phone, exception);
            return NotificationDeliveryResult.failed(exception.getMessage());
        }
    }

    private String normalizePhone(String phone) {
        if (phone == null) {
            return "";
        }
        String digits = phone.replaceAll("\\D", "");
        if (digits.startsWith("0")) {
            return "62" + digits.substring(1);
        }
        if (digits.startsWith("8")) {
            return "62" + digits;
        }
        return digits;
    }
}

record GatewaySendRequest(String phone, String message) {
}

record GatewaySendResponse(boolean success, String message) {
}
