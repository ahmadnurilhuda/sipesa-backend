package com.pomosda.permission.student;

import com.pomosda.permission.common.CurrentUser;
import com.pomosda.permission.exception.ApiException;
import com.pomosda.permission.parent.ParentGuardianRepository;
import com.pomosda.permission.parent.ParentGuardian;
import com.pomosda.permission.room.Room;
import com.pomosda.permission.room.RoomRepository;
import com.pomosda.permission.schoolclass.SchoolClass;
import com.pomosda.permission.schoolclass.SchoolClassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudentService {
    private final StudentRepository repository;
    private final SchoolClassRepository classRepository;
    private final RoomRepository roomRepository;
    private final ParentGuardianRepository parentRepository;
    private final CurrentUser currentUser;
    private final StudentAccessService studentAccessService;

    @Transactional(readOnly = true)
    public List<StudentDto> all(Authentication authentication) {
        var user = currentUser.get(authentication);
        return repository.findAll().stream()
                .filter(student -> studentAccessService.canView(student, user))
                .map(StudentDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public StudentDto get(UUID id, Authentication authentication) {
        Student student = entity(id);
        if (!studentAccessService.canView(student, currentUser.get(authentication))) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Tidak berhak melihat data santri ini");
        }
        return StudentDto.from(student);
    }

    @Transactional
    public StudentDto create(StudentRequest request) {
        Student student = new Student();
        apply(student, request);
        return StudentDto.from(repository.save(student));
    }

    @Transactional
    public StudentDto update(UUID id, StudentRequest request) {
        Student student = entity(id);
        apply(student, request);
        return StudentDto.from(repository.save(student));
    }

    @Transactional
    public void delete(UUID id) {
        repository.delete(entity(id));
    }

    private void apply(Student student, StudentRequest request) {
        student.setNis(request.nis());
        student.setName(request.name());
        student.setGender(request.gender());
        student.setActive(request.active());
        student.setSchoolClass(findClass(request.classId()));
        student.setRoom(findRoom(request.roomId()));
        student.setParentGuardian(findParentGuardian(request.parentGuardianId()));
    }

    private SchoolClass findClass(UUID classId) {
        if (classId == null) {
            return null;
        }
        return classRepository.findById(classId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Kelas tidak ditemukan"));
    }

    private Room findRoom(UUID roomId) {
        if (roomId == null) {
            return null;
        }
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Kamar tidak ditemukan"));
    }

    private ParentGuardian findParentGuardian(UUID parentGuardianId) {
        if (parentGuardianId == null) {
            return null;
        }
        return parentRepository.findById(parentGuardianId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Wali santri tidak ditemukan"));
    }

    private Student entity(UUID id) {
        return repository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Santri tidak ditemukan"));
    }
}
