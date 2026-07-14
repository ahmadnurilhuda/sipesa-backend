package com.pomosda.permission.history;

import java.time.LocalDate;
import java.util.UUID;

public record StudentRoomHistoryDto(
        UUID id,
        UUID studentId,
        String studentName,
        UUID roomId,
        String roomName,
        UUID academicYearId,
        String academicYear,
        LocalDate startDate,
        LocalDate endDate,
        boolean active
) {
    public static StudentRoomHistoryDto from(StudentRoomHistory history) {
        return new StudentRoomHistoryDto(
                history.getId(),
                history.getStudent().getId(),
                history.getStudent().getName(),
                history.getRoom().getId(),
                history.getRoom().getName(),
                history.getAcademicYear() == null ? null : history.getAcademicYear().getId(),
                history.getAcademicYear() == null ? null : history.getAcademicYear().getPeriod(),
                history.getStartDate(),
                history.getEndDate(),
                history.isActive()
        );
    }
}
