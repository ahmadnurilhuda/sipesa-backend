package com.pomosda.permission.dashboard;

import com.pomosda.permission.common.CurrentUser;
import com.pomosda.permission.permission.PermissionDto;
import com.pomosda.permission.permission.PermissionRequest;
import com.pomosda.permission.permission.PermissionRequestRepository;
import com.pomosda.permission.permission.PermissionService;
import com.pomosda.permission.permission.PermissionStatus;
import com.pomosda.permission.student.StudentAccessService;
import com.pomosda.permission.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','KAUR_ASRAMA','WALI_KELAS','WALI_KAMAR')")
public class DashboardController {
    private final PermissionRequestRepository repository;
    private final PermissionService permissionService;
    private final CurrentUser currentUser;
    private final StudentAccessService studentAccessService;

    @GetMapping("/summary")
    @Transactional
    DashboardSummary summary(Authentication authentication) {
        permissionService.markOverdue();
        User user = currentUser.get(authentication);
        Instant start = Instant.now().truncatedTo(ChronoUnit.DAYS);
        Instant end = start.plus(1, ChronoUnit.DAYS);
        List<PermissionRequest> permissions = scopedPermissions(user);
        long overdue = countByStatus(permissions, PermissionStatus.OVERDUE);
        return new DashboardSummary(
                countByStatus(permissions, PermissionStatus.APPROVED),
                countByStatus(permissions, PermissionStatus.CHECKED_OUT) + overdue,
                permissions.stream()
                        .filter(permission -> permission.getStatus() == PermissionStatus.COMPLETED)
                        .filter(permission -> !permission.getUpdatedAt().isBefore(start) && permission.getUpdatedAt().isBefore(end))
                        .count(),
                overdue,
                countByStatus(permissions, PermissionStatus.PENDING_WALI_KELAS),
                countByStatus(permissions, PermissionStatus.PENDING_WALI_KAMAR)
        );
    }

    @GetMapping("/active-permissions")
    @Transactional(readOnly = true)
    List<PermissionDto> active(Authentication authentication) {
        User user = currentUser.get(authentication);
        return repository.findTop20ByStatusOrderByCreatedAtDesc(PermissionStatus.CHECKED_OUT).stream()
                .filter(permission -> studentAccessService.canMonitorPermission(permission.getStudent(), user))
                .map(PermissionDto::from)
                .toList();
    }

    @GetMapping("/overdue")
    @Transactional(readOnly = true)
    List<PermissionDto> overdue(Authentication authentication) {
        User user = currentUser.get(authentication);
        return repository.findTop20ByStatusOrderByCreatedAtDesc(PermissionStatus.OVERDUE).stream()
                .filter(permission -> studentAccessService.canMonitorPermission(permission.getStudent(), user))
                .map(PermissionDto::from)
                .toList();
    }

    private List<PermissionRequest> scopedPermissions(User user) {
        return repository.findAll().stream()
                .filter(permission -> studentAccessService.canMonitorPermission(permission.getStudent(), user))
                .toList();
    }

    private long countByStatus(List<PermissionRequest> permissions, PermissionStatus status) {
        return permissions.stream().filter(permission -> permission.getStatus() == status).count();
    }
}

record DashboardSummary(long totalApprovedPermissions, long totalCheckedOutStudents, long totalCompletedToday, long totalOverdueStudents, long pendingWaliKelas, long pendingWaliKamar) {
}
