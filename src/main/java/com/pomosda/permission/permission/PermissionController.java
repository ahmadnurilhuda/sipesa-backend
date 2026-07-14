package com.pomosda.permission.permission;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {
    private final PermissionService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('WALI_SANTRI','ADMIN')")
    PermissionDto create(@Valid @RequestBody PermissionCreateRequest request, Authentication authentication) {
        return service.create(request, authentication);
    }

    @GetMapping
    List<PermissionDto> all(Authentication authentication) {
        return service.all(authentication);
    }

    @GetMapping("/{id}")
    PermissionDto get(@PathVariable UUID id, Authentication authentication) {
        return service.get(id, authentication);
    }

    @PostMapping("/{id}/approve-wali-kelas")
    @PreAuthorize("hasAnyRole('WALI_KELAS','ADMIN')")
    PermissionDto approveWaliKelas(@PathVariable UUID id, @RequestBody DecisionRequest request, Authentication authentication) {
        return service.approveWaliKelas(id, request, authentication);
    }

    @PostMapping("/{id}/reject-wali-kelas")
    @PreAuthorize("hasAnyRole('WALI_KELAS','ADMIN')")
    PermissionDto rejectWaliKelas(@PathVariable UUID id, @RequestBody DecisionRequest request, Authentication authentication) {
        return service.rejectWaliKelas(id, request, authentication);
    }

    @PostMapping("/{id}/approve-wali-kamar")
    @PreAuthorize("hasAnyRole('WALI_KAMAR','ADMIN')")
    PermissionDto approveWaliKamar(@PathVariable UUID id, @RequestBody DecisionRequest request, Authentication authentication) {
        return service.approveWaliKamar(id, request, authentication);
    }

    @PostMapping("/{id}/reject-wali-kamar")
    @PreAuthorize("hasAnyRole('WALI_KAMAR','ADMIN')")
    PermissionDto rejectWaliKamar(@PathVariable UUID id, @RequestBody DecisionRequest request, Authentication authentication) {
        return service.rejectWaliKamar(id, request, authentication);
    }

    @PostMapping("/{id}/confirm-return")
    @PreAuthorize("hasAnyRole('WALI_KAMAR','ADMIN')")
    PermissionDto confirmReturn(@PathVariable UUID id, @RequestBody DecisionRequest request, Authentication authentication) {
        return service.confirmReturn(id, request, authentication);
    }

    @PostMapping("/{id}/cancel")
    PermissionDto cancel(@PathVariable UUID id, Authentication authentication) {
        return service.cancel(id, authentication);
    }

    @GetMapping("/{id}/qr")
    QrTokenResponse qr(@PathVariable UUID id, Authentication authentication) {
        return service.qr(id, authentication);
    }
}
