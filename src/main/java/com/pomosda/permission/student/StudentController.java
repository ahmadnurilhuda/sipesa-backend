package com.pomosda.permission.student;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','WALI_SANTRI','WALI_KELAS','WALI_KAMAR','KAUR_ASRAMA')")
public class StudentController {
    private final StudentService service;

    @GetMapping
    List<StudentDto> all(Authentication authentication) {
        return service.all(authentication);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    StudentDto create(@Valid @RequestBody StudentRequest request) {
        return service.create(request);
    }

    @GetMapping("/{id}")
    StudentDto get(@PathVariable UUID id, Authentication authentication) {
        return service.get(id, authentication);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    StudentDto update(@PathVariable UUID id, @Valid @RequestBody StudentRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}
