package com.pomosda.permission.qrcode;

import com.pomosda.permission.common.BaseEntity;
import com.pomosda.permission.permission.PermissionRequest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "permission_qr_tokens")
public class PermissionQrToken extends BaseEntity {
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_request_id", nullable = false)
    private PermissionRequest permissionRequest;

    @Column(nullable = false, unique = true, length = 96)
    private String token;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean active = true;
}
