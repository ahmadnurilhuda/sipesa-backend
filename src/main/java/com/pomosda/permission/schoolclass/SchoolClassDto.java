package com.pomosda.permission.schoolclass;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record SchoolClassDto(UUID id, String name, String schoolYear, UUID academicYearId) {
    public static SchoolClassDto from(SchoolClass schoolClass) {
        return new SchoolClassDto(
                schoolClass.getId(),
                schoolClass.getName(),
                schoolClass.getSchoolYear(),
                schoolClass.getAcademicYear() == null ? null : schoolClass.getAcademicYear().getId()
        );
    }
}

record SchoolClassRequest(@NotBlank String name, UUID academicYearId) {
}
