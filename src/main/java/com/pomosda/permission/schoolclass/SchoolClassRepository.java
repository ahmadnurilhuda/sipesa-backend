package com.pomosda.permission.schoolclass;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SchoolClassRepository extends JpaRepository<SchoolClass, UUID> {
    @Override
    @EntityGraph(attributePaths = {"homeroomTeacher", "academicYear"})
    List<SchoolClass> findAll();

    @Override
    @EntityGraph(attributePaths = {"homeroomTeacher", "academicYear"})
    Optional<SchoolClass> findById(UUID id);
}
