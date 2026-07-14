package com.pomosda.permission.pengurus;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PengurusRepository extends JpaRepository<Pengurus, UUID> {
    @Override
    @EntityGraph(attributePaths = "user")
    List<Pengurus> findAll();

    @Override
    @EntityGraph(attributePaths = "user")
    Optional<Pengurus> findById(UUID id);

    boolean existsByPhone(String phone);

    boolean existsByPhoneAndIdNot(String phone, UUID id);
}
