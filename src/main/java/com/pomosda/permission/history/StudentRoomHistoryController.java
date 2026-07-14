package com.pomosda.permission.history;

import com.pomosda.permission.academicyear.AcademicYearRepository;
import com.pomosda.permission.exception.ApiException;
import com.pomosda.permission.room.Room;
import com.pomosda.permission.room.RoomRepository;
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
@RequestMapping("/api/student-room-histories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class StudentRoomHistoryController {
    private final StudentRoomHistoryRepository repository;
    private final StudentRepository studentRepository;
    private final RoomRepository roomRepository;
    private final AcademicYearRepository academicYearRepository;

    @GetMapping
    List<StudentRoomHistoryDto> all() {
        return repository.findAll().stream().map(StudentRoomHistoryDto::from).toList();
    }

    @PostMapping
    @Transactional
    StudentRoomHistoryDto create(@Valid @RequestBody StudentRoomHistoryRequest request) {
        Student student = studentRepository.findById(request.studentId()).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Santri tidak ditemukan"));
        Room room = roomRepository.findById(request.roomId()).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Kamar tidak ditemukan"));
        closeActiveHistories(student.getId(), request.startDate());
        StudentRoomHistory history = new StudentRoomHistory();
        apply(history, request, student, room);
        student.setRoom(room);
        return StudentRoomHistoryDto.from(repository.save(history));
    }

    @GetMapping("/{id}")
    StudentRoomHistoryDto get(@PathVariable UUID id) {
        return StudentRoomHistoryDto.from(entity(id));
    }

    @PutMapping("/{id}")
    @Transactional
    StudentRoomHistoryDto update(@PathVariable UUID id, @Valid @RequestBody StudentRoomHistoryRequest request) {
        StudentRoomHistory history = entity(id);
        Student student = studentRepository.findById(request.studentId()).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Santri tidak ditemukan"));
        Room room = roomRepository.findById(request.roomId()).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Kamar tidak ditemukan"));
        apply(history, request, student, room);
        if (history.isActive()) {
            student.setRoom(room);
        }
        return StudentRoomHistoryDto.from(repository.save(history));
    }

    @DeleteMapping("/{id}")
    void delete(@PathVariable UUID id) {
        repository.delete(entity(id));
    }

    private void apply(StudentRoomHistory history, StudentRoomHistoryRequest request, Student student, Room room) {
        history.setStudent(student);
        history.setRoom(room);
        history.setAcademicYear(request.academicYearId() == null ? null : academicYearRepository.findById(request.academicYearId()).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Tahun ajaran tidak ditemukan")));
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

    private StudentRoomHistory entity(UUID id) {
        return repository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Riwayat kamar santri tidak ditemukan"));
    }
}
