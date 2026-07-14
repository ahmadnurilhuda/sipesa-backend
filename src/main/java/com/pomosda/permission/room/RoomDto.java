package com.pomosda.permission.room;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record RoomDto(UUID id, String name, String building) {
    public static RoomDto from(Room room) {
        return new RoomDto(
                room.getId(),
                room.getName(),
                room.getBuilding()
        );
    }
}

record RoomRequest(@NotBlank String name, String building) {
}
