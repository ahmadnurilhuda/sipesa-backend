package com.pomosda.permission.permission;

import com.pomosda.permission.common.BaseEntity;
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

@Getter
@Setter
@Entity
@Table(name = "permission_approval_logs")
public class PermissionApprovalLog extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_request_id", nullable = false)
    private PermissionRequest permissionRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", nullable = false)
    private User actor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PermissionStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PermissionStatus toStatus;

    @Column(columnDefinition = "text")
    private String note;
}
