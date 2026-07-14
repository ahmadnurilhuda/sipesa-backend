package com.pomosda.permission.room;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, UUID> {
    @Override
    @EntityGraph(attributePaths = "guardian")
    List<Room> findAll();

    @Override
    @EntityGraph(attributePaths = "guardian")
    Optional<Room> findById(UUID id);
}
