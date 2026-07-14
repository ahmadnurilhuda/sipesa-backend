package com.pomosda.permission.guardian;

import java.util.UUID;

public record ClassGuardianDto(UUID id, UUID pengurusId, String pengurusName, UUID classId, String className, UUID academicYearId, String academicYear) {
    public static ClassGuardianDto from(ClassGuardian guardian) {
        return new ClassGuardianDto(
                guardian.getId(),
                guardian.getPengurus().getId(),
                guardian.getPengurus().getName(),
                guardian.getSchoolClass().getId(),
                guardian.getSchoolClass().getName(),
                guardian.getAcademicYear().getId(),
                guardian.getAcademicYear().getPeriod()
        );
    }
}
