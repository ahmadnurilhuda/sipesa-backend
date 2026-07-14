package com.pomosda.permission.history;

import java.time.LocalDate;
import java.util.UUID;

public record StudentClassHistoryDto(
        UUID id,
        UUID studentId,
        String studentName,
        UUID classId,
        String className,
        UUID academicYearId,
        String academicYear,
        LocalDate startDate,
        LocalDate endDate,
        boolean active
) {
    public static StudentClassHistoryDto from(StudentClassHistory history) {
        return new StudentClassHistoryDto(
                history.getId(),
                history.getStudent().getId(),
                history.getStudent().getName(),
                history.getSchoolClass().getId(),
                history.getSchoolClass().getName(),
                history.getAcademicYear().getId(),
                history.getAcademicYear().getPeriod(),
                history.getStartDate(),
                history.getEndDate(),
                history.isActive()
        );
    }
}
