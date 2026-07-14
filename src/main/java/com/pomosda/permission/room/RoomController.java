package com.pomosda.permission.room;

import com.pomosda.permission.exception.ApiException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class RoomController {
    private final RoomRepository repository;

    @GetMapping
    List<RoomDto> all() {
        return repository.findAll().stream().map(RoomDto::from).toList();
    }

    @PostMapping
    RoomDto create(@Valid @RequestBody RoomRequest request) {
        Room entity = new Room();
        apply(entity, request);
        return RoomDto.from(repository.save(entity));
    }

    @GetMapping("/{id}")
    RoomDto get(@PathVariable UUID id) {
        return RoomDto.from(entity(id));
    }

    @PutMapping("/{id}")
    RoomDto update(@PathVariable UUID id, @Valid @RequestBody RoomRequest request) {
        Room entity = entity(id);
        apply(entity, request);
        return RoomDto.from(repository.save(entity));
    }

    @DeleteMapping("/{id}")
    void delete(@PathVariable UUID id) {
        repository.delete(entity(id));
    }

    private void apply(Room entity, RoomRequest request) {
        entity.setName(request.name());
        entity.setBuilding(request.building());
    }

    private Room entity(UUID id) {
        return repository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Kamar tidak ditemukan"));
    }
}
