package com.pomosda.permission.permission;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PermissionDto(
        UUID id,
        UUID studentId,
        String studentName,
        String permissionType,
        String reason,
        String destination,
        Instant startAt,
        Instant expectedReturnAt,
        Instant checkedOutAt,
        Instant checkedInAt,
        PermissionStatus status,
        List<PermissionApprovalLogDto> approvalLogs
) {
    public static PermissionDto from(PermissionRequest p) {
        return from(p, List.of());
    }

    public static PermissionDto from(PermissionRequest p, List<PermissionApprovalLog> logs) {
        return new PermissionDto(
                p.getId(),
                p.getStudent().getId(),
                p.getStudent().getName(),
                p.getPermissionType(),
                p.getReason(),
                p.getDestination(),
                p.getStartAt(),
                p.getExpectedReturnAt(),
                p.getCheckedOutAt(),
                p.getCheckedInAt(),
                p.getStatus(),
                logs.stream().map(PermissionApprovalLogDto::from).toList()
        );
    }
}

record PermissionApprovalLogDto(
        UUID id,
        String actorName,
        String actorRole,
        PermissionStatus fromStatus,
        PermissionStatus toStatus,
        String note,
        Instant createdAt
) {
    static PermissionApprovalLogDto from(PermissionApprovalLog log) {
        return new PermissionApprovalLogDto(
                log.getId(),
                log.getActor().getName(),
                log.getActor().getRole().name(),
                log.getFromStatus(),
                log.getToStatus(),
                log.getNote(),
                log.getCreatedAt()
        );
    }
}

record PermissionCreateRequest(
        @NotNull UUID studentId,
        @NotBlank String permissionType,
        @NotBlank String reason,
        @NotBlank String destination,
        @NotNull Instant startAt,
        @NotNull @Future Instant expectedReturnAt
) {
}

record DecisionRequest(String note) {
}

record QrTokenResponse(String token, Instant expiresAt) {
}

record ScanRequest(@NotBlank String token) {
}
