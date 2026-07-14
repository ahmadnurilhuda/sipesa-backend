package com.pomosda.permission.semester;

import com.pomosda.permission.academicyear.AcademicYearRepository;
import com.pomosda.permission.exception.ApiException;
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
@RequestMapping("/api/semesters")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SemesterController {
    private final SemesterRepository repository;
    private final AcademicYearRepository academicYearRepository;

    @GetMapping
    @Transactional(readOnly = true)
    List<SemesterDto> all() {
        return repository.findAll().stream().map(SemesterDto::from).toList();
    }

    @PostMapping
    @Transactional
    SemesterDto create(@Valid @RequestBody SemesterRequest request) {
        Semester semester = new Semester();
        apply(semester, request);
        return SemesterDto.from(repository.save(semester));
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    SemesterDto get(@PathVariable UUID id) {
        return SemesterDto.from(entity(id));
    }

    @PutMapping("/{id}")
    @Transactional
    SemesterDto update(@PathVariable UUID id, @Valid @RequestBody SemesterRequest request) {
        Semester semester = entity(id);
        apply(semester, request);
        return SemesterDto.from(repository.save(semester));
    }

    @DeleteMapping("/{id}")
    void delete(@PathVariable UUID id) {
        repository.delete(entity(id));
    }

    private void apply(Semester semester, SemesterRequest request) {
        if (request.endDate().isBefore(request.startDate())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Tanggal selesai semester tidak boleh sebelum tanggal mulai");
        }
        semester.setAcademicYear(academicYearRepository.findById(request.academicYearId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Tahun ajaran tidak ditemukan")));
        semester.setName(request.name());
        semester.setStartDate(request.startDate());
        semester.setEndDate(request.endDate());
        semester.setMaxPermissionDays(request.maxPermissionDays());
        semester.setActive(request.active());
    }

    private Semester entity(UUID id) {
        return repository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Semester tidak ditemukan"));
    }
}
