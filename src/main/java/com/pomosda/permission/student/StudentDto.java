package com.pomosda.permission.student;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record StudentDto(
        UUID id,
        String nis,
        String name,
        StudentGender gender,
        boolean active,
        UUID classId,
        String className,
        UUID roomId,
        String roomName,
        UUID parentGuardianId,
        String parentGuardianName
) {
    public static StudentDto from(Student s) {
        return new StudentDto(
                s.getId(),
                s.getNis(),
                s.getName(),
                s.getGender(),
                s.isActive(),
                s.getSchoolClass() == null ? null : s.getSchoolClass().getId(),
                s.getSchoolClass() == null ? null : s.getSchoolClass().getName(),
                s.getRoom() == null ? null : s.getRoom().getId(),
                s.getRoom() == null ? null : s.getRoom().getName(),
                s.getParentGuardian() == null ? null : s.getParentGuardian().getId(),
                s.getParentGuardian() == null ? null : s.getParentGuardian().getName()
        );
    }
}

record StudentRequest(@NotBlank String nis, @NotBlank String name, StudentGender gender, boolean active, UUID classId, UUID roomId, UUID parentGuardianId) {
}
