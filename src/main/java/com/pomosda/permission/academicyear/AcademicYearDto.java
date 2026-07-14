package com.pomosda.permission.academicyear;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record AcademicYearDto(UUID id, String period, boolean active) {
    public static AcademicYearDto from(AcademicYear academicYear) {
        return new AcademicYearDto(academicYear.getId(), academicYear.getPeriod(), academicYear.isActive());
    }
}

record AcademicYearRequest(@NotBlank String period, boolean active) {
}
