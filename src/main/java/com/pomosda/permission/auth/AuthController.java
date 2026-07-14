package com.pomosda.permission.auth;

import com.pomosda.permission.common.CurrentUser;
import com.pomosda.permission.exception.ApiException;
import com.pomosda.permission.security.JwtService;
import com.pomosda.permission.user.Role;
import com.pomosda.permission.user.User;
import com.pomosda.permission.user.UserDto;
import com.pomosda.permission.user.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUser currentUser;
    private final ForgotPasswordService forgotPasswordService;

    @PostMapping("/login")
    AuthResponse login(@Valid @RequestBody LoginRequest request) {
        String email = request.email();
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.password()));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Login gagal"));
        return new AuthResponse(jwtService.generate(user), UserDto.from(user));
    }

    @PostMapping("/register-admin")
    AuthResponse registerAdmin(@Valid @RequestBody RegisterAdminRequest request) {
        if (userRepository.existsByUsernameOrEmail(request.username(), request.email())) {
            throw new ApiException(HttpStatus.CONFLICT, "Admin sudah tersedia");
        }
        User user = new User();
        user.setName(request.name());
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setRole(Role.ADMIN);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        userRepository.save(user);
        return new AuthResponse(jwtService.generate(user), UserDto.from(user));
    }

    @PostMapping("/refresh")
    AuthResponse refresh(Authentication authentication) {
        User user = currentUser.get(authentication);
        return new AuthResponse(jwtService.generate(user), UserDto.from(user));
    }

    @PostMapping("/logout")
    void logout() {
    }

    @PostMapping("/forgot-password/request-otp")
    ForgotPasswordMessage requestForgotPasswordOtp(@Valid @RequestBody ForgotPasswordOtpRequest request) {
        return forgotPasswordService.requestOtp(request);
    }

    @PostMapping("/forgot-password/verify-otp")
    ForgotPasswordVerifyResponse verifyForgotPasswordOtp(@Valid @RequestBody ForgotPasswordVerifyRequest request) {
        return forgotPasswordService.verifyOtp(request);
    }

    @PostMapping("/forgot-password/reset")
    ForgotPasswordMessage resetForgotPassword(@Valid @RequestBody ForgotPasswordResetRequest request) {
        return forgotPasswordService.resetPassword(request);
    }

    @GetMapping("/me")
    UserDto me(Authentication authentication) {
        return UserDto.from(currentUser.get(authentication));
    }

    @PutMapping("/me")
    AuthResponse updateMe(@Valid @RequestBody UpdateProfileRequest request, Authentication authentication) {
        User user = currentUser.get(authentication);
        if (userRepository.existsByUsernameAndIdNot(request.username(), user.getId())) {
            throw new ApiException(HttpStatus.CONFLICT, "Username sudah digunakan");
        }
        if (userRepository.existsByEmailAndIdNot(request.email(), user.getId())) {
            throw new ApiException(HttpStatus.CONFLICT, "Email sudah digunakan");
        }
        if (request.phone() != null && !request.phone().isBlank() && userRepository.existsByPhoneAndIdNot(request.phone(), user.getId())) {
            throw new ApiException(HttpStatus.CONFLICT, "No. HP sudah dipakai");
        }
        if (request.newPassword() != null && !request.newPassword().isBlank()) {
            if (request.currentPassword() == null || request.currentPassword().isBlank()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Kata sandi lama wajib diisi");
            }
            if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Kata sandi lama tidak sesuai");
            }
            user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        }
        user.setName(request.name());
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPhone(request.phone());
        User saved = userRepository.save(user);
        return new AuthResponse(jwtService.generate(saved), UserDto.from(saved));
    }
}

record LoginRequest(@NotBlank String email, @NotBlank String password) {
}

record RegisterAdminRequest(@NotBlank String name, @NotBlank String username, @NotBlank String email, @NotBlank String password) {
}

record UpdateProfileRequest(
        @NotBlank String name,
        @NotBlank String username,
        @NotBlank @Email String email,
        String phone,
        String currentPassword,
        @Size(min = 6) String newPassword
) {
}

record AuthResponse(String accessToken, UserDto user) {
}

record ForgotPasswordOtpRequest(@NotBlank @Email String email) {
}

record ForgotPasswordVerifyRequest(@NotBlank @Email String email, @NotBlank @Size(min = 6, max = 6) String otp) {
}

record ForgotPasswordResetRequest(@NotBlank @Email String email, @NotBlank String resetToken, @NotBlank @Size(min = 6) String newPassword) {
}

record ForgotPasswordVerifyResponse(String resetToken, String message) {
}

record ForgotPasswordMessage(String message) {
}
