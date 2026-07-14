package com.pomosda.permission.schoolclass;

import com.pomosda.permission.academicyear.AcademicYear;
import com.pomosda.permission.academicyear.AcademicYearRepository;
import com.pomosda.permission.exception.ApiException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
public class SchoolClassController {
    private final SchoolClassRepository repository;
    private final AcademicYearRepository academicYearRepository;

    @GetMapping
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ADMIN','KAUR_ASRAMA')")
    List<SchoolClassDto> all() {
        return repository.findAll().stream().map(SchoolClassDto::from).toList();
    }

    @PostMapping
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    SchoolClassDto create(@Valid @RequestBody SchoolClassRequest request) {
        SchoolClass entity = new SchoolClass();
        apply(entity, request);
        return SchoolClassDto.from(repository.save(entity));
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ADMIN','KAUR_ASRAMA')")
    SchoolClassDto get(@PathVariable UUID id) {
        return SchoolClassDto.from(entity(id));
    }

    @PutMapping("/{id}")
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    SchoolClassDto update(@PathVariable UUID id, @Valid @RequestBody SchoolClassRequest request) {
        SchoolClass entity = entity(id);
        apply(entity, request);
        return SchoolClassDto.from(repository.save(entity));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    void delete(@PathVariable UUID id) {
        repository.delete(entity(id));
    }

    private void apply(SchoolClass entity, SchoolClassRequest request) {
        AcademicYear academicYear = academicYearRepository.findById(request.academicYearId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Tahun ajaran tidak ditemukan"));
        entity.setName(request.name());
        entity.setAcademicYear(academicYear);
        entity.setSchoolYear(academicYear.getPeriod());
    }

    private SchoolClass entity(UUID id) {
        return repository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Kelas tidak ditemukan"));
    }
}
