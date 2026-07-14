package com.pomosda.permission.parent;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record ParentGuardianDto(UUID id, String name, String phone, String address, UUID userId, String userName, String userEmail, String userPhone) {
    public static ParentGuardianDto from(ParentGuardian parent) {
        return new ParentGuardianDto(
                parent.getId(),
                parent.getName(),
                parent.getPhone(),
                parent.getAddress(),
                parent.getUser() == null ? null : parent.getUser().getId(),
                parent.getUser() == null ? null : parent.getUser().getName(),
                parent.getUser() == null ? null : parent.getUser().getEmail(),
                parent.getUser() == null ? null : parent.getUser().getPhone()
        );
    }
}

record ParentGuardianRequest(@NotBlank String name, String phone, String address, UUID userId) {
}
