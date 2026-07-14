package com.pomosda.permission.permission;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PermissionApprovalLogRepository extends JpaRepository<PermissionApprovalLog, UUID> {
    @Query("""
            select log from PermissionApprovalLog log
            join fetch log.actor
            where log.permissionRequest.id = :permissionRequestId
            order by log.createdAt asc
            """)
    List<PermissionApprovalLog> findTimeline(@Param("permissionRequestId") UUID permissionRequestId);
}
