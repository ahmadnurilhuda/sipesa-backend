package com.pomosda.permission.auth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PasswordResetOtpRepository extends JpaRepository<PasswordResetOtp, UUID> {
    Optional<PasswordResetOtp> findTopByEmailAndUsedAtIsNullOrderByCreatedAtDesc(String email);
}
