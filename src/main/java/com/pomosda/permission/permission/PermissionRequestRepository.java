package com.pomosda.permission.permission;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PermissionRequestRepository extends JpaRepository<PermissionRequest, UUID>, JpaSpecificationExecutor<PermissionRequest> {
    String[] PERMISSION_GRAPH = {
            "student",
            "student.schoolClass",
            "student.schoolClass.academicYear",
            "student.schoolClass.homeroomTeacher",
            "student.room",
            "student.room.guardian",
            "student.parentGuardian",
            "student.parentGuardian.user",
            "semester",
            "semester.academicYear",
            "requestedBy"
    };

    @Override
    @EntityGraph(attributePaths = {
            "student",
            "student.schoolClass",
            "student.schoolClass.academicYear",
            "student.schoolClass.homeroomTeacher",
            "student.room",
            "student.room.guardian",
            "student.parentGuardian",
            "student.parentGuardian.user",
            "semester",
            "semester.academicYear",
            "requestedBy"
    })
    List<PermissionRequest> findAll();

    @Override
    @EntityGraph(attributePaths = {
            "student",
            "student.schoolClass",
            "student.schoolClass.academicYear",
            "student.schoolClass.homeroomTeacher",
            "student.room",
            "student.room.guardian",
            "student.parentGuardian",
            "student.parentGuardian.user",
            "semester",
            "semester.academicYear",
            "requestedBy"
    })
    Optional<PermissionRequest> findById(UUID id);

    long countByStatus(PermissionStatus status);
    long countByStatusAndUpdatedAtBetween(PermissionStatus status, Instant start, Instant end);

    @EntityGraph(attributePaths = {
            "student",
            "student.schoolClass",
            "student.schoolClass.academicYear",
            "student.schoolClass.homeroomTeacher",
            "student.room",
            "student.room.guardian",
            "student.parentGuardian",
            "student.parentGuardian.user",
            "semester",
            "semester.academicYear",
            "requestedBy"
    })
    List<PermissionRequest> findTop20ByStatusOrderByCreatedAtDesc(PermissionStatus status);
}
