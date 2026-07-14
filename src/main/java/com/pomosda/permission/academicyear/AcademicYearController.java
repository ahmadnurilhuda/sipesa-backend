package com.pomosda.permission.academicyear;

import com.pomosda.permission.exception.ApiException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/academic-years")
@RequiredArgsConstructor
public class AcademicYearController {
    private final AcademicYearRepository repository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','KAUR_ASRAMA')")
    List<AcademicYearDto> all() {
        return repository.findAll().stream().map(AcademicYearDto::from).toList();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    AcademicYearDto create(@Valid @RequestBody AcademicYearRequest request) {
        AcademicYear academicYear = new AcademicYear();
        apply(academicYear, request);
        return AcademicYearDto.from(repository.save(academicYear));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','KAUR_ASRAMA')")
    AcademicYearDto get(@PathVariable UUID id) {
        return AcademicYearDto.from(entity(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    AcademicYearDto update(@PathVariable UUID id, @Valid @RequestBody AcademicYearRequest request) {
        AcademicYear academicYear = entity(id);
        apply(academicYear, request);
        return AcademicYearDto.from(repository.save(academicYear));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    void delete(@PathVariable UUID id) {
        repository.delete(entity(id));
    }

    private void apply(AcademicYear academicYear, AcademicYearRequest request) {
        academicYear.setPeriod(request.period());
        academicYear.setActive(request.active());
    }

    private AcademicYear entity(UUID id) {
        return repository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Tahun ajaran tidak ditemukan"));
    }
}
