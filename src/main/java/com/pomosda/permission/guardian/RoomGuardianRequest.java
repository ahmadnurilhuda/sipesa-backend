package com.pomosda.permission.guardian;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

record RoomGuardianRequest(@NotNull UUID pengurusId, @NotNull UUID roomId, @NotNull UUID academicYearId) {
}
