package com.pomosda.permission.pengurus;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record PengurusDto(UUID id, String name, String nip, String phone, String position, UUID userId, String userName, String userEmail, String userPhone) {
    public static PengurusDto from(Pengurus pengurus) {
        return new PengurusDto(
                pengurus.getId(),
                pengurus.getName(),
                pengurus.getNip(),
                pengurus.getPhone(),
                pengurus.getPosition(),
                pengurus.getUser() == null ? null : pengurus.getUser().getId(),
                pengurus.getUser() == null ? null : pengurus.getUser().getName(),
                pengurus.getUser() == null ? null : pengurus.getUser().getEmail(),
                pengurus.getUser() == null ? null : pengurus.getUser().getPhone()
        );
    }
}

record PengurusRequest(@NotBlank String name, String nip, String phone, @NotBlank String position, UUID userId) {
}
