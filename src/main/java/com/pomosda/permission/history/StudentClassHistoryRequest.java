package com.pomosda.permission.history;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

record StudentClassHistoryRequest(@NotNull UUID studentId, @NotNull UUID classId, @NotNull UUID academicYearId, LocalDate startDate, LocalDate endDate, Boolean active) {
    StudentClassHistoryRequest {
        active = active == null ? true : active;
        startDate = startDate == null ? LocalDate.now() : startDate;
    }
}
