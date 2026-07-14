package com.pomosda.permission.student;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StudentRepository extends JpaRepository<Student, UUID> {
    @Override
    @EntityGraph(attributePaths = {"schoolClass", "room", "parentGuardian"})
    List<Student> findAll();

    @Override
    @EntityGraph(attributePaths = {"schoolClass", "room", "parentGuardian"})
    Optional<Student> findById(UUID id);
}
