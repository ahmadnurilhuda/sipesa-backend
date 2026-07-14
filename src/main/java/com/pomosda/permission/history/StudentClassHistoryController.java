package com.pomosda.permission.history;

import com.pomosda.permission.academicyear.AcademicYearRepository;
import com.pomosda.permission.exception.ApiException;
import com.pomosda.permission.schoolclass.SchoolClass;
import com.pomosda.permission.schoolclass.SchoolClassRepository;
import com.pomosda.permission.student.Student;
import com.pomosda.permission.student.StudentRepository;
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
@RequestMapping("/api/student-class-histories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class StudentClassHistoryController {
    private final StudentClassHistoryRepository repository;
    private final StudentRepository studentRepository;
    private final SchoolClassRepository classRepository;
    private final AcademicYearRepository academicYearRepository;

    @GetMapping
    List<StudentClassHistoryDto> all() {
        return repository.findAll().stream().map(StudentClassHistoryDto::from).toList();
    }

    @PostMapping
    @Transactional
    StudentClassHistoryDto create(@Valid @RequestBody StudentClassHistoryRequest request) {
        Student student = studentRepository.findById(request.studentId()).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Santri tidak ditemukan"));
        SchoolClass schoolClass = classRepository.findById(request.classId()).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Kelas tidak ditemukan"));
        closeActiveHistories(student.getId(), request.startDate());
        StudentClassHistory history = new StudentClassHistory();
        apply(history, request, student, schoolClass);
        student.setSchoolClass(schoolClass);
        return StudentClassHistoryDto.from(repository.save(history));
    }

    @GetMapping("/{id}")
    StudentClassHistoryDto get(@PathVariable UUID id) {
        return StudentClassHistoryDto.from(entity(id));
    }

    @PutMapping("/{id}")
    @Transactional
    StudentClassHistoryDto update(@PathVariable UUID id, @Valid @RequestBody StudentClassHistoryRequest request) {
        StudentClassHistory history = entity(id);
        Student student = studentRepository.findById(request.studentId()).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Santri tidak ditemukan"));
        SchoolClass schoolClass = classRepository.findById(request.classId()).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Kelas tidak ditemukan"));
        apply(history, request, student, schoolClass);
        if (history.isActive()) {
            student.setSchoolClass(schoolClass);
        }
        return StudentClassHistoryDto.from(repository.save(history));
    }

    @DeleteMapping("/{id}")
    void delete(@PathVariable UUID id) {
        repository.delete(entity(id));
    }

    private void apply(StudentClassHistory history, StudentClassHistoryRequest request, Student student, SchoolClass schoolClass) {
        history.setStudent(student);
        history.setSchoolClass(schoolClass);
        history.setAcademicYear(academicYearRepository.findById(request.academicYearId()).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Tahun ajaran tidak ditemukan")));
        history.setStartDate(request.startDate());
        history.setEndDate(request.endDate());
        history.setActive(request.active());
    }

    private void closeActiveHistories(UUID studentId, java.time.LocalDate newStartDate) {
        repository.findByStudentIdAndActiveTrue(studentId).forEach(history -> {
            history.setActive(false);
            history.setEndDate(newStartDate == null ? java.time.LocalDate.now() : newStartDate.minusDays(1));
        });
    }

    private StudentClassHistory entity(UUID id) {
        return repository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Riwayat kelas santri tidak ditemukan"));
    }
}
