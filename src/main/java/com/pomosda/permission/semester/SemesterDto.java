package com.pomosda.permission.semester;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record SemesterDto(
        UUID id,
        UUID academicYearId,
        String academicYear,
        SemesterName name,
        LocalDate startDate,
        LocalDate endDate,
        int maxPermissionDays,
        boolean active
) {
    public static SemesterDto from(Semester semester) {
        return new SemesterDto(
                semester.getId(),
                semester.getAcademicYear().getId(),
                semester.getAcademicYear().getPeriod(),
                semester.getName(),
                semester.getStartDate(),
                semester.getEndDate(),
                semester.getMaxPermissionDays(),
                semester.isActive()
        );
    }
}

record SemesterRequest(
        @NotNull UUID academicYearId,
        @NotNull SemesterName name,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @Min(1) int maxPermissionDays,
        boolean active
) {
}
