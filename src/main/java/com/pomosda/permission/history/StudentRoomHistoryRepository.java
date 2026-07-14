package com.pomosda.permission.history;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StudentRoomHistoryRepository extends JpaRepository<StudentRoomHistory, UUID> {
    @Override
    @EntityGraph(attributePaths = {"student", "room", "academicYear"})
    List<StudentRoomHistory> findAll();

    @Override
    @EntityGraph(attributePaths = {"student", "room", "academicYear"})
    Optional<StudentRoomHistory> findById(UUID id);

    List<StudentRoomHistory> findByStudentIdAndActiveTrue(UUID studentId);
}
