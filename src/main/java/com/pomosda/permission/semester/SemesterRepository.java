package com.pomosda.permission.semester;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SemesterRepository extends JpaRepository<Semester, UUID> {
    Optional<Semester> findFirstByStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByStartDateDesc(LocalDate startDate, LocalDate endDate);

    List<Semester> findByAcademicYearIdOrderByStartDateAsc(UUID academicYearId);
}
