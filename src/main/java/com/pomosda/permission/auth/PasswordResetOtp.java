package com.pomosda.permission.auth;

import com.pomosda.permission.common.BaseEntity;
import com.pomosda.permission.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "password_reset_otps")
public class PasswordResetOtp extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String codeHash;

    private String resetTokenHash;

    @Column(nullable = false)
    private Instant expiresAt;

    private Instant verifiedAt;
    private Instant usedAt;

    @Column(nullable = false)
    private int attemptCount = 0;
}
