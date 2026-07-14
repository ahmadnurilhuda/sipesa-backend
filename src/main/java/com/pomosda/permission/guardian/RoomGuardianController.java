package com.pomosda.permission.guardian;

import com.pomosda.permission.academicyear.AcademicYearRepository;
import com.pomosda.permission.exception.ApiException;
import com.pomosda.permission.pengurus.PengurusRepository;
import com.pomosda.permission.room.RoomRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/room-guardians")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class RoomGuardianController {
    private final RoomGuardianRepository repository;
    private final PengurusRepository pengurusRepository;
    private final RoomRepository roomRepository;
    private final AcademicYearRepository academicYearRepository;

    @GetMapping
    @Transactional(readOnly = true)
    List<RoomGuardianDto> all() {
        return repository.findAll().stream().map(RoomGuardianDto::from).toList();
    }

    @PostMapping
    @Transactional
    RoomGuardianDto create(@Valid @RequestBody RoomGuardianRequest request) {
        RoomGuardian guardian = new RoomGuardian();
        apply(guardian, request);
        return RoomGuardianDto.from(repository.save(guardian));
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    RoomGuardianDto get(@PathVariable UUID id) {
        return RoomGuardianDto.from(entity(id));
    }

    @PutMapping("/{id}")
    @Transactional
    RoomGuardianDto update(@PathVariable UUID id, @Valid @RequestBody RoomGuardianRequest request) {
        RoomGuardian guardian = entity(id);
        apply(guardian, request);
        return RoomGuardianDto.from(repository.save(guardian));
    }

    @DeleteMapping("/{id}")
    void delete(@PathVariable UUID id) {
        repository.delete(entity(id));
    }

    private void apply(RoomGuardian guardian, RoomGuardianRequest request) {
        guardian.setPengurus(pengurusRepository.findById(request.pengurusId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Pengurus tidak ditemukan")));
        guardian.setRoom(roomRepository.findById(request.roomId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Kamar tidak ditemukan")));
        guardian.setAcademicYear(academicYearRepository.findById(request.academicYearId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Tahun ajaran tidak ditemukan")));
    }

    private RoomGuardian entity(UUID id) {
        return repository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Wali kamar tidak ditemukan"));
    }
}
