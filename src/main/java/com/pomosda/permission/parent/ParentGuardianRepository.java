package com.pomosda.permission.parent;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ParentGuardianRepository extends JpaRepository<ParentGuardian, UUID> {
    @Override
    @EntityGraph(attributePaths = "user")
    List<ParentGuardian> findAll();

    @Override
    @EntityGraph(attributePaths = "user")
    Optional<ParentGuardian> findById(UUID id);

    boolean existsByUserId(UUID userId);

    boolean existsByUserIdAndIdNot(UUID userId, UUID id);

    boolean existsByPhone(String phone);

    boolean existsByPhoneAndIdNot(String phone, UUID id);
}
