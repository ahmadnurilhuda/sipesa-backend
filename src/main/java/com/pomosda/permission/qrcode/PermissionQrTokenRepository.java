package com.pomosda.permission.qrcode;

import com.pomosda.permission.permission.PermissionRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PermissionQrTokenRepository extends JpaRepository<PermissionQrToken, UUID> {
    Optional<PermissionQrToken> findByPermissionRequest(PermissionRequest permissionRequest);
    Optional<PermissionQrToken> findByTokenAndActiveTrue(String token);
}
