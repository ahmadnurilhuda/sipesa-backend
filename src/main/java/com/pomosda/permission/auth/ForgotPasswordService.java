package com.pomosda.permission.auth;

import com.pomosda.permission.exception.ApiException;
import com.pomosda.permission.notification.NotificationEmailService;
import com.pomosda.permission.user.User;
import com.pomosda.permission.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class ForgotPasswordService {
    private static final int OTP_TTL_MINUTES = 5;
    private static final int RESET_TOKEN_TTL_MINUTES = 10;
    private static final int MAX_ATTEMPTS = 5;

    private final UserRepository userRepository;
    private final PasswordResetOtpRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationEmailService notificationEmailService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public ForgotPasswordMessage requestOtp(ForgotPasswordOtpRequest request) {
        String email = normalizeEmail(request.email());
        User user = userRepository.findByEmail(email)
                .filter(User::isActive)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Email tidak dikenal"));

        String code = generateOtp();
        PasswordResetOtp otp = new PasswordResetOtp();
        otp.setUser(user);
        otp.setEmail(email);
        otp.setCodeHash(passwordEncoder.encode(code));
        otp.setExpiresAt(Instant.now().plusSeconds(OTP_TTL_MINUTES * 60L));
        otpRepository.save(otp);
        notificationEmailService.sendOtp(user, code);
        return new ForgotPasswordMessage("Kode OTP dikirim melalui email.");
    }

    @Transactional
    public ForgotPasswordVerifyResponse verifyOtp(ForgotPasswordVerifyRequest request) {
        PasswordResetOtp otp = activeOtp(request.email());
        if (otp.getAttemptCount() >= MAX_ATTEMPTS) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "Percobaan OTP terlalu banyak. Minta kode baru.");
        }
        otp.setAttemptCount(otp.getAttemptCount() + 1);
        if (!passwordEncoder.matches(request.otp(), otp.getCodeHash())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "OTP salah");
        }
        String resetToken = randomToken();
        otp.setVerifiedAt(Instant.now());
        otp.setExpiresAt(Instant.now().plusSeconds(RESET_TOKEN_TTL_MINUTES * 60L));
        otp.setResetTokenHash(passwordEncoder.encode(resetToken));
        return new ForgotPasswordVerifyResponse(resetToken, "OTP benar. Silakan buat kata sandi baru.");
    }

    @Transactional
    public ForgotPasswordMessage resetPassword(ForgotPasswordResetRequest request) {
        PasswordResetOtp otp = activeOtp(request.email());
        if (otp.getVerifiedAt() == null || otp.getResetTokenHash() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "OTP belum diverifikasi");
        }
        if (!passwordEncoder.matches(request.resetToken(), otp.getResetTokenHash())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Token reset tidak valid");
        }
        otp.getUser().setPasswordHash(passwordEncoder.encode(request.newPassword()));
        otp.setUsedAt(Instant.now());
        return new ForgotPasswordMessage("Kata sandi berhasil diperbarui.");
    }

    private PasswordResetOtp activeOtp(String rawEmail) {
        String email = normalizeEmail(rawEmail);
        PasswordResetOtp otp = otpRepository.findTopByEmailAndUsedAtIsNullOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Kode OTP tidak ditemukan"));
        if (otp.getExpiresAt().isBefore(Instant.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "OTP kedaluwarsa");
        }
        return otp;
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private String generateOtp() {
        return String.format("%06d", secureRandom.nextInt(1_000_000));
    }

    private String randomToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }
}
