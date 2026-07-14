package com.pomosda.permission.guardian;

import com.pomosda.permission.academicyear.AcademicYearRepository;
import com.pomosda.permission.exception.ApiException;
import com.pomosda.permission.pengurus.PengurusRepository;
import com.pomosda.permission.schoolclass.SchoolClassRepository;
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
@RequestMapping("/api/class-guardians")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ClassGuardianController {
    private final ClassGuardianRepository repository;
    private final PengurusRepository pengurusRepository;
    private final SchoolClassRepository classRepository;
    private final AcademicYearRepository academicYearRepository;

    @GetMapping
    @Transactional(readOnly = true)
    List<ClassGuardianDto> all() {
        return repository.findAll().stream().map(ClassGuardianDto::from).toList();
    }

    @PostMapping
    @Transactional
    ClassGuardianDto create(@Valid @RequestBody ClassGuardianRequest request) {
        ClassGuardian guardian = new ClassGuardian();
        apply(guardian, request);
        return ClassGuardianDto.from(repository.save(guardian));
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    ClassGuardianDto get(@PathVariable UUID id) {
        return ClassGuardianDto.from(entity(id));
    }

    @PutMapping("/{id}")
    @Transactional
    ClassGuardianDto update(@PathVariable UUID id, @Valid @RequestBody ClassGuardianRequest request) {
        ClassGuardian guardian = entity(id);
        apply(guardian, request);
        return ClassGuardianDto.from(repository.save(guardian));
    }

    @DeleteMapping("/{id}")
    void delete(@PathVariable UUID id) {
        repository.delete(entity(id));
    }

    private void apply(ClassGuardian guardian, ClassGuardianRequest request) {
        guardian.setPengurus(pengurusRepository.findById(request.pengurusId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Pengurus tidak ditemukan")));
        guardian.setSchoolClass(classRepository.findById(request.classId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Kelas tidak ditemukan")));
        guardian.setAcademicYear(academicYearRepository.findById(request.academicYearId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Tahun ajaran tidak ditemukan")));
    }

    private ClassGuardian entity(UUID id) {
        return repository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Wali kelas tidak ditemukan"));
    }
}
