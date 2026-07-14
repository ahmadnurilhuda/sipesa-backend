package com.pomosda.permission.history;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

record StudentRoomHistoryRequest(@NotNull UUID studentId, @NotNull UUID roomId, UUID academicYearId, LocalDate startDate, LocalDate endDate, Boolean active) {
    StudentRoomHistoryRequest {
        active = active == null ? true : active;
        startDate = startDate == null ? LocalDate.now() : startDate;
    }
}
