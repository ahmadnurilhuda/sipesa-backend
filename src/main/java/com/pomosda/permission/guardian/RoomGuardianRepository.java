package com.pomosda.permission.guardian;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoomGuardianRepository extends JpaRepository<RoomGuardian, UUID> {
    @EntityGraph(attributePaths = {"pengurus.user", "room", "academicYear"})
    Optional<RoomGuardian> findFirstByRoomIdAndAcademicYearActiveTrue(UUID roomId);
}
