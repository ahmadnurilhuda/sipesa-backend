package com.pomosda.permission.permission;

import com.pomosda.permission.common.BaseEntity;
import com.pomosda.permission.semester.Semester;
import com.pomosda.permission.student.Student;
import com.pomosda.permission.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "permission_requests")
public class PermissionRequest extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by_id", nullable = false)
    private User requestedBy;

    @Column(nullable = false)
    private String permissionType;

    @Column(nullable = false, columnDefinition = "text")
    private String reason;

    @Column(nullable = false)
    private String destination;

    @Column(nullable = false)
    private Instant startAt;

    @Column(nullable = false)
    private Instant expectedReturnAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "semester_id")
    private Semester semester;

    private Instant checkedOutAt;
    private Instant checkedInAt;
    private Instant completedAt;
    private Instant returnReminderSentAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PermissionStatus status = PermissionStatus.PENDING_WALI_KELAS;
}
