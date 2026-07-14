package com.pomosda.permission.history;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StudentClassHistoryRepository extends JpaRepository<StudentClassHistory, UUID> {
    @Override
    @EntityGraph(attributePaths = {"student", "schoolClass", "academicYear"})
    List<StudentClassHistory> findAll();

    @Override
    @EntityGraph(attributePaths = {"student", "schoolClass", "academicYear"})
    Optional<StudentClassHistory> findById(UUID id);

    List<StudentClassHistory> findByStudentIdAndActiveTrue(UUID studentId);
}
