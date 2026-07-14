package com.pomosda.permission.student;

import com.pomosda.permission.guardian.ClassGuardianRepository;
import com.pomosda.permission.guardian.RoomGuardianRepository;
import com.pomosda.permission.schoolclass.SchoolClass;
import com.pomosda.permission.room.Room;
import com.pomosda.permission.user.Role;
import com.pomosda.permission.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudentAccessService {
    private final ClassGuardianRepository classGuardianRepository;
    private final RoomGuardianRepository roomGuardianRepository;

    public boolean canView(Student student, User user) {
        return switch (user.getRole()) {
            case ADMIN, KAUR_ASRAMA -> true;
            case KEAMANAN -> false;
            case WALI_SANTRI -> isParentGuardian(student, user);
            case WALI_KELAS -> isActiveClassGuardian(student.getSchoolClass(), user);
            case WALI_KAMAR -> isActiveRoomGuardian(student.getRoom(), user);
        };
    }

    public boolean canSubmitPermission(Student student, User user) {
        return user.getRole() == Role.ADMIN || (user.getRole() == Role.WALI_SANTRI && isParentGuardian(student, user));
    }

    public boolean canApproveAsClassGuardian(Student student, User user) {
        return user.getRole() == Role.ADMIN || (user.getRole() == Role.WALI_KELAS && isActiveClassGuardian(student.getSchoolClass(), user));
    }

    public boolean canApproveAsRoomGuardian(Student student, User user) {
        return user.getRole() == Role.ADMIN || (user.getRole() == Role.WALI_KAMAR && isActiveRoomGuardian(student.getRoom(), user));
    }

    public boolean canMonitorPermission(Student student, User user) {
        return user.getRole() == Role.KEAMANAN || canView(student, user);
    }

    private boolean isParentGuardian(Student student, User user) {
        return student.getParentGuardian() != null
                && student.getParentGuardian().getUser() != null
                && student.getParentGuardian().getUser().getId().equals(user.getId());
    }

    private boolean isActiveClassGuardian(SchoolClass schoolClass, User user) {
        if (schoolClass == null) {
            return false;
        }
        return classGuardianRepository.findFirstBySchoolClassIdAndAcademicYearActiveTrue(schoolClass.getId())
                .map(guardian -> guardian.getPengurus().getUser())
                .map(guardianUser -> guardianUser != null && guardianUser.getId().equals(user.getId()))
                .orElse(false);
    }

    private boolean isActiveRoomGuardian(Room room, User user) {
        if (room == null) {
            return false;
        }
        return roomGuardianRepository.findFirstByRoomIdAndAcademicYearActiveTrue(room.getId())
                .map(guardian -> guardian.getPengurus().getUser())
                .map(guardianUser -> guardianUser != null && guardianUser.getId().equals(user.getId()))
                .orElse(false);
    }
}
