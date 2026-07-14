package com.pomosda.permission.guardian;

import java.util.UUID;

public record RoomGuardianDto(UUID id, UUID pengurusId, String pengurusName, UUID roomId, String roomName, UUID academicYearId, String academicYear) {
    public static RoomGuardianDto from(RoomGuardian guardian) {
        return new RoomGuardianDto(
                guardian.getId(),
                guardian.getPengurus().getId(),
                guardian.getPengurus().getName(),
                guardian.getRoom().getId(),
                guardian.getRoom().getName(),
                guardian.getAcademicYear().getId(),
                guardian.getAcademicYear().getPeriod()
        );
    }
}
