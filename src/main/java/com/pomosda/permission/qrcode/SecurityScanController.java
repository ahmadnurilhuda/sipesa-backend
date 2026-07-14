package com.pomosda.permission.qrcode;

import com.pomosda.permission.permission.PermissionDto;
import com.pomosda.permission.permission.PermissionService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/security/scan")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('KEAMANAN','ADMIN')")
public class SecurityScanController {
    private final PermissionService service;

    @PostMapping("/preview")
    PermissionDto preview(@RequestBody ScanPayload request) {
        return service.scanPreview(request.token());
    }

    @PostMapping("/check-out")
    PermissionDto checkOut(@RequestBody ScanPayload request, Authentication authentication) {
        return service.scanCheckOut(request.token(), authentication);
    }

    @PostMapping("/check-in")
    PermissionDto checkIn(@RequestBody ScanPayload request, Authentication authentication) {
        return service.scanCheckIn(request.token(), authentication);
    }
}

record ScanPayload(@NotBlank String token) {
}
