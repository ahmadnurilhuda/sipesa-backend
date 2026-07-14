package com.pomosda.permission.guardian;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ClassGuardianRepository extends JpaRepository<ClassGuardian, UUID> {
    @EntityGraph(attributePaths = {"pengurus.user", "schoolClass", "academicYear"})
    Optional<ClassGuardian> findFirstBySchoolClassIdAndAcademicYearActiveTrue(UUID schoolClassId);
}
