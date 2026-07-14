package com.pomosda.permission.notification;

public record NotificationDeliveryResult(NotificationDeliveryStatus status, String error) {
    public static NotificationDeliveryResult sent() {
        return new NotificationDeliveryResult(NotificationDeliveryStatus.SENT, null);
    }

    public static NotificationDeliveryResult disabled(String channel) {
        return new NotificationDeliveryResult(NotificationDeliveryStatus.DISABLED, channel + " belum diaktifkan");
    }

    public static NotificationDeliveryResult noEmail() {
        return new NotificationDeliveryResult(NotificationDeliveryStatus.NO_EMAIL, "Email penerima belum tersedia");
    }

    public static NotificationDeliveryResult noPhone() {
        return new NotificationDeliveryResult(NotificationDeliveryStatus.NO_PHONE, "Nomor WhatsApp penerima belum tersedia");
    }

    public static NotificationDeliveryResult failed(String error) {
        return new NotificationDeliveryResult(NotificationDeliveryStatus.FAILED, error);
    }
}
