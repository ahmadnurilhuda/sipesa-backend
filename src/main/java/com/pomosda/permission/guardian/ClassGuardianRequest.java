package com.pomosda.permission.guardian;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

record ClassGuardianRequest(@NotNull UUID pengurusId, @NotNull UUID classId, @NotNull UUID academicYearId) {
}
