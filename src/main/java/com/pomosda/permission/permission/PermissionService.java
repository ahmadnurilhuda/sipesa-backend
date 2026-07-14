package com.pomosda.permission.permission;

import com.pomosda.permission.common.CurrentUser;
import com.pomosda.permission.exception.ApiException;
import com.pomosda.permission.guardian.ClassGuardianRepository;
import com.pomosda.permission.guardian.RoomGuardianRepository;
import com.pomosda.permission.notification.NotificationService;
import com.pomosda.permission.qrcode.PermissionQrToken;
import com.pomosda.permission.qrcode.PermissionQrTokenRepository;
import com.pomosda.permission.room.Room;
import com.pomosda.permission.schoolclass.SchoolClass;
import com.pomosda.permission.semester.Semester;
import com.pomosda.permission.semester.SemesterRepository;
import com.pomosda.permission.student.Student;
import com.pomosda.permission.student.StudentAccessService;
import com.pomosda.permission.student.StudentRepository;
import com.pomosda.permission.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PermissionService {
    private static final ZoneId JAKARTA_ZONE = ZoneId.of("Asia/Jakarta");
    private static final DateTimeFormatter MESSAGE_DATE_TIME = DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm 'WIB'").withZone(JAKARTA_ZONE);
    private static final long RETURN_REMINDER_SECONDS = 2 * 60 * 60;
    private static final Set<PermissionStatus> QUOTA_STATUSES = EnumSet.of(
            PermissionStatus.PENDING_WALI_KELAS,
            PermissionStatus.PENDING_WALI_KAMAR,
            PermissionStatus.APPROVED,
            PermissionStatus.CHECKED_OUT,
            PermissionStatus.CHECKED_IN,
            PermissionStatus.COMPLETED,
            PermissionStatus.OVERDUE
    );

    private final PermissionRequestRepository repository;
    private final PermissionApprovalLogRepository logRepository;
    private final PermissionQrTokenRepository tokenRepository;
    private final StudentRepository studentRepository;
    private final StudentAccessService studentAccessService;
    private final SemesterRepository semesterRepository;
    private final CurrentUser currentUser;
    private final NotificationService notificationService;
    private final ClassGuardianRepository classGuardianRepository;
    private final RoomGuardianRepository roomGuardianRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    private enum ApprovalScope {
        CLASS,
        ROOM
    }

    public List<PermissionDto> all(Authentication authentication) {
        markOverdue();
        User user = currentUser.get(authentication);
        return repository.findAll().stream()
                .filter(permission -> canView(permission, user))
                .map(PermissionDto::from)
                .toList();
    }

    public PermissionDto get(UUID id, Authentication authentication) {
        PermissionRequest permission = entity(id);
        requireCanView(permission, currentUser.get(authentication));
        return PermissionDto.from(permission, logRepository.findTimeline(permission.getId()));
    }

    @Transactional
    public PermissionDto create(PermissionCreateRequest request, Authentication authentication) {
        User user = currentUser.get(authentication);
        Student student = studentRepository.findById(request.studentId()).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Santri tidak ditemukan"));
        Semester semester = validateCreateRequest(request, student, user);
        PermissionRequest permission = new PermissionRequest();
        permission.setStudent(student);
        permission.setRequestedBy(user);
        permission.setPermissionType(request.permissionType());
        permission.setReason(request.reason());
        permission.setDestination(request.destination());
        permission.setStartAt(request.startAt());
        permission.setExpectedReturnAt(request.expectedReturnAt());
        permission.setSemester(semester);
        permission.setStatus(PermissionStatus.PENDING_WALI_KELAS);
        PermissionRequest saved = repository.save(permission);
        notificationService.create(
                activeClassGuardianUser(student.getSchoolClass()),
                "Pengajuan Izin Baru",
                permissionMessage(saved, "Tahap saat ini: menunggu persetujuan Wali Kelas.", "Mohon periksa dan berikan keputusan melalui aplikasi SIPESA.", null)
        );
        return PermissionDto.from(saved);
    }

    private Semester validateCreateRequest(PermissionCreateRequest request, Student student, User user) {
        if (!request.expectedReturnAt().isAfter(request.startAt())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Estimasi kembali harus setelah waktu mulai");
        }
        if (!studentAccessService.canSubmitPermission(student, user)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Tidak berhak membuat izin untuk santri ini");
        }
        Semester semester = semesterForRequest(request);
        validateSemesterQuota(request, student, semester);
        return semester;
    }

    private Semester semesterForRequest(PermissionCreateRequest request) {
        LocalDate startDate = request.startAt().atZone(JAKARTA_ZONE).toLocalDate();
        LocalDate returnDate = request.expectedReturnAt().atZone(JAKARTA_ZONE).toLocalDate();
        Semester semester = semesterRepository.findFirstByStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByStartDateDesc(startDate, startDate)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Tanggal izin tidak masuk ke semester yang terdaftar"));
        if (returnDate.isAfter(semester.getEndDate())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Tanggal kembali izin harus masih berada dalam semester yang sama. Semester "
                    + semester.getName() + " berakhir pada " + semester.getEndDate());
        }
        return semester;
    }

    private void validateSemesterQuota(PermissionCreateRequest request, Student student, Semester semester) {
        long requestedDays = permissionDays(request.startAt(), request.expectedReturnAt());
        long usedDays = repository.findAll().stream()
                .filter(permission -> permission.getStudent().getId().equals(student.getId()))
                .filter(permission -> permission.getSemester() != null && permission.getSemester().getId().equals(semester.getId()))
                .filter(permission -> QUOTA_STATUSES.contains(permission.getStatus()))
                .mapToLong(permission -> permissionDays(permission.getStartAt(), permission.getExpectedReturnAt()))
                .sum();
        long remainingDays = semester.getMaxPermissionDays() - usedDays;
        if (requestedDays > remainingDays) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Sisa izin semester ini tinggal " + Math.max(0, remainingDays)
                    + " hari. Durasi pengajuan ini " + requestedDays
                    + " hari, sehingga melebihi batas izin " + semester.getMaxPermissionDays() + " hari per semester.");
        }
    }

    private long permissionDays(Instant startAt, Instant expectedReturnAt) {
        LocalDate startDate = startAt.atZone(JAKARTA_ZONE).toLocalDate();
        LocalDate returnDate = expectedReturnAt.atZone(JAKARTA_ZONE).toLocalDate();
        return Math.max(1, ChronoUnit.DAYS.between(startDate, returnDate) + 1);
    }

    @Transactional
    public PermissionDto approveWaliKelas(UUID id, DecisionRequest request, Authentication authentication) {
        PermissionRequest permission = transition(id, PermissionStatus.PENDING_WALI_KELAS, PermissionStatus.PENDING_WALI_KAMAR, request.note(), authentication, ApprovalScope.CLASS);
        notificationService.create(
                activeRoomGuardianUser(permission.getStudent().getRoom()),
                "Izin Menunggu Wali Kamar",
                permissionMessage(permission, "Wali Kelas telah menyetujui pengajuan izin.", "Tahap berikutnya: menunggu keputusan Wali Kamar.", request.note())
        );
        return PermissionDto.from(permission);
    }

    @Transactional
    public PermissionDto rejectWaliKelas(UUID id, DecisionRequest request, Authentication authentication) {
        requireDecisionNote(request.note(), "Alasan penolakan wajib diisi");
        PermissionRequest permission = transition(id, PermissionStatus.PENDING_WALI_KELAS, PermissionStatus.REJECTED_BY_WALI_KELAS, request.note(), authentication, ApprovalScope.CLASS);
        notificationService.create(
                permission.getRequestedBy(),
                "Izin Ditolak Wali Kelas",
                permissionMessage(permission, "Pengajuan izin ditolak oleh Wali Kelas.", "Silakan lihat detail izin di aplikasi SIPESA.", request.note())
        );
        return PermissionDto.from(permission);
    }

    @Transactional
    public PermissionDto approveWaliKamar(UUID id, DecisionRequest request, Authentication authentication) {
        PermissionRequest permission = transition(id, PermissionStatus.PENDING_WALI_KAMAR, PermissionStatus.APPROVED, request.note(), authentication, ApprovalScope.ROOM);
        createToken(permission);
        notificationService.create(
                permission.getRequestedBy(),
                "Izin Disetujui",
                permissionMessage(permission, "Pengajuan izin sudah disetujui Wali Kamar.", "QR Code e-ticket sudah tersedia di aplikasi SIPESA dan hanya dapat digunakan sesuai jadwal izin.", request.note())
        );
        return PermissionDto.from(permission);
    }

    @Transactional
    public PermissionDto rejectWaliKamar(UUID id, DecisionRequest request, Authentication authentication) {
        requireDecisionNote(request.note(), "Alasan penolakan wajib diisi");
        PermissionRequest permission = transition(id, PermissionStatus.PENDING_WALI_KAMAR, PermissionStatus.REJECTED_BY_WALI_KAMAR, request.note(), authentication, ApprovalScope.ROOM);
        notificationService.create(
                permission.getRequestedBy(),
                "Izin Ditolak Wali Kamar",
                permissionMessage(permission, "Pengajuan izin ditolak oleh Wali Kamar.", "Silakan lihat detail izin di aplikasi SIPESA.", request.note())
        );
        return PermissionDto.from(permission);
    }

    @Transactional
    public PermissionDto confirmReturn(UUID id, DecisionRequest request, Authentication authentication) {
        PermissionRequest permission = transition(id, PermissionStatus.CHECKED_IN, PermissionStatus.COMPLETED, request.note(), authentication, ApprovalScope.ROOM);
        permission.setCompletedAt(Instant.now());
        tokenRepository.findByPermissionRequest(permission).ifPresent(token -> token.setActive(false));
        notificationService.create(
                permission.getRequestedBy(),
                "Izin Selesai",
                permissionMessage(permission, "Izin santri telah selesai dan sudah dikonfirmasi oleh Wali Kamar.",
                        "Waktu selesai: " + formatInstant(permission.getCompletedAt()) + ". Terima kasih sudah mengikuti alur perizinan SIPESA.", request.note())
        );
        return PermissionDto.from(permission);
    }

    @Transactional
    public PermissionDto cancel(UUID id, Authentication authentication) {
        PermissionRequest permission = entity(id);
        PermissionStatus old = permission.getStatus();
        permission.setStatus(PermissionStatus.CANCELLED);
        log(permission, old, PermissionStatus.CANCELLED, "Dibatalkan", currentUser.get(authentication));
        tokenRepository.findByPermissionRequest(permission).ifPresent(token -> token.setActive(false));
        return PermissionDto.from(repository.save(permission));
    }

    public QrTokenResponse qr(UUID id, Authentication authentication) {
        PermissionRequest permission = entity(id);
        requireCanView(permission, currentUser.get(authentication));
        PermissionQrToken token = tokenRepository.findByPermissionRequest(permission).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "QR belum tersedia"));
        return new QrTokenResponse(token.getToken(), token.getExpiresAt());
    }

    @Transactional
    public PermissionDto scanPreview(String rawToken) {
        PermissionQrToken token = validToken(rawToken);
        return PermissionDto.from(token.getPermissionRequest());
    }

    @Transactional
    public PermissionDto scanCheckOut(String rawToken, Authentication authentication) {
        PermissionQrToken token = validToken(rawToken);
        PermissionRequest permission = token.getPermissionRequest();
        requireStatus(permission, PermissionStatus.APPROVED);
        Instant now = Instant.now();
        if (now.isBefore(permission.getStartAt())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Izin belum dapat digunakan. Waktu izin mulai: " + permission.getStartAt());
        }
        permission.setStatus(PermissionStatus.CHECKED_OUT);
        permission.setCheckedOutAt(now);
        log(permission, PermissionStatus.APPROVED, PermissionStatus.CHECKED_OUT, "Scan keluar", currentUser.get(authentication));
        notificationService.create(
                permission.getRequestedBy(),
                "Santri Keluar",
                permissionMessage(permission, "Santri sudah check-out dari area pondok.", "Waktu keluar tercatat: " + formatInstant(permission.getCheckedOutAt()) + ".", null)
        );
        return PermissionDto.from(repository.save(permission));
    }

    @Transactional
    public PermissionDto scanCheckIn(String rawToken, Authentication authentication) {
        PermissionQrToken token = validToken(rawToken);
        PermissionRequest permission = token.getPermissionRequest();
        requireStatus(permission, PermissionStatus.CHECKED_OUT);
        permission.setStatus(PermissionStatus.CHECKED_IN);
        permission.setCheckedInAt(Instant.now());
        log(permission, PermissionStatus.CHECKED_OUT, PermissionStatus.CHECKED_IN, "Scan kembali", currentUser.get(authentication));
        notificationService.create(
                permission.getRequestedBy(),
                "Santri Kembali",
                permissionMessage(permission, "Santri sudah check-in kembali ke pondok.", "Waktu kembali tercatat: " + formatInstant(permission.getCheckedInAt()) + ". Menunggu konfirmasi Wali Kamar untuk penyelesaian izin.", null)
        );
        notificationService.create(
                activeRoomGuardianUser(permission.getStudent().getRoom()),
                "Santri Sudah Check-in",
                permissionMessage(permission, "Santri sudah masuk area pondok setelah scan check-in oleh petugas keamanan.",
                        "Mohon Wali Kamar melakukan konfirmasi fisik setelah santri bertemu langsung atau sudah kembali ke kamar.", null)
        );
        return PermissionDto.from(repository.save(permission));
    }

    @Scheduled(fixedDelayString = "${app.permission.return-reminder-interval-ms:300000}")
    @Transactional
    public void sendReturnReminders() {
        Instant now = Instant.now();
        Instant reminderWindow = now.plusSeconds(RETURN_REMINDER_SECONDS);
        repository.findAll().stream()
                .filter(permission -> permission.getStatus() == PermissionStatus.CHECKED_OUT)
                .filter(permission -> permission.getReturnReminderSentAt() == null)
                .filter(permission -> !permission.getExpectedReturnAt().isAfter(reminderWindow))
                .forEach(permission -> {
                    permission.setReturnReminderSentAt(now);
                    notificationService.create(
                            permission.getRequestedBy(),
                            "Pengingat Kembali ke Pondok",
                            permissionMessage(permission,
                                    "Pengingat: waktu kembali santri ke pondok sudah dekat.",
                                    "Mohon memastikan santri kembali ke pondok paling lambat " + formatInstant(permission.getExpectedReturnAt()) + ".", null)
                    );
                });
    }

    @Transactional
    public void markOverdue() {
        Instant now = Instant.now();
        repository.findAll().stream()
                .filter(p -> p.getStatus() == PermissionStatus.CHECKED_OUT && p.getExpectedReturnAt().isBefore(now))
                .forEach(p -> {
                    p.setStatus(PermissionStatus.OVERDUE);
                    notificationService.create(
                            p.getRequestedBy(),
                            "Santri Terlambat",
                            permissionMessage(p, "Santri melewati batas estimasi kembali.", "Batas kembali: " + formatInstant(p.getExpectedReturnAt()) + ". Mohon segera melakukan pengecekan.", null)
                    );
                });
    }

    private PermissionRequest transition(UUID id, PermissionStatus from, PermissionStatus to, String note, Authentication authentication, ApprovalScope scope) {
        PermissionRequest permission = entity(id);
        User actor = currentUser.get(authentication);
        requireCanAct(permission, actor, scope);
        requireStatus(permission, from);
        permission.setStatus(to);
        log(permission, from, to, note, actor);
        return repository.save(permission);
    }

    private void createToken(PermissionRequest permission) {
        PermissionQrToken token = tokenRepository.findByPermissionRequest(permission).orElseGet(PermissionQrToken::new);
        token.setPermissionRequest(permission);
        token.setToken(randomToken());
        token.setExpiresAt(permission.getExpectedReturnAt().plusSeconds(86400));
        token.setActive(true);
        tokenRepository.save(token);
    }

    private String randomToken() {
        byte[] bytes = new byte[48];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private PermissionQrToken validToken(String rawToken) {
        PermissionQrToken token = tokenRepository.findByTokenAndActiveTrue(rawToken)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Token QR tidak valid"));
        if (token.getExpiresAt().isBefore(Instant.now())) {
            token.setActive(false);
            throw new ApiException(HttpStatus.BAD_REQUEST, "Token QR kedaluwarsa");
        }
        return token;
    }

    private void requireStatus(PermissionRequest permission, PermissionStatus expected) {
        if (permission.getStatus() != expected) {
            throw new ApiException(HttpStatus.CONFLICT, "Status izin tidak sesuai. Saat ini: " + permission.getStatus());
        }
    }

    private void requireDecisionNote(String note, String message) {
        if (note == null || note.trim().length() < 3) {
            throw new ApiException(HttpStatus.BAD_REQUEST, message);
        }
    }

    private String permissionMessage(PermissionRequest permission, String event, String instruction, String note) {
        StringBuilder builder = new StringBuilder();
        builder.append(event).append("\n\n");
        builder.append("Detail izin:\n");
        builder.append("- Santri: ").append(permission.getStudent().getName()).append("\n");
        builder.append("- Jenis izin: ").append(permission.getPermissionType()).append("\n");
        builder.append("- Tujuan: ").append(permission.getDestination()).append("\n");
        builder.append("- Mulai: ").append(formatInstant(permission.getStartAt())).append("\n");
        builder.append("- Estimasi kembali: ").append(formatInstant(permission.getExpectedReturnAt())).append("\n");
        builder.append("- Status: ").append(permission.getStatus()).append("\n");
        builder.append("- Alasan pengajuan: ").append(permission.getReason());
        if (note != null && !note.isBlank()) {
            builder.append("\n\nCatatan:\n").append(note.trim());
        }
        if (instruction != null && !instruction.isBlank()) {
            builder.append("\n\n").append(instruction);
        }
        return builder.toString();
    }

    private String formatInstant(Instant instant) {
        return instant == null ? "-" : MESSAGE_DATE_TIME.format(instant);
    }

    private void log(PermissionRequest permission, PermissionStatus from, PermissionStatus to, String note, User actor) {
        PermissionApprovalLog log = new PermissionApprovalLog();
        log.setPermissionRequest(permission);
        log.setActor(actor);
        log.setFromStatus(from);
        log.setToStatus(to);
        log.setNote(note);
        logRepository.save(log);
    }

    private PermissionRequest entity(UUID id) {
        return repository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Izin tidak ditemukan"));
    }

    private void requireCanView(PermissionRequest permission, User user) {
        if (!canView(permission, user)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Tidak berhak melihat izin ini");
        }
    }

    private void requireCanAct(PermissionRequest permission, User user, ApprovalScope scope) {
        boolean allowed = switch (scope) {
            case CLASS -> studentAccessService.canApproveAsClassGuardian(permission.getStudent(), user);
            case ROOM -> studentAccessService.canApproveAsRoomGuardian(permission.getStudent(), user);
        };
        if (!allowed) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Tidak memiliki akses untuk memproses izin santri ini");
        }
    }

    private boolean canView(PermissionRequest permission, User user) {
        return studentAccessService.canMonitorPermission(permission.getStudent(), user);
    }

    private User activeClassGuardianUser(SchoolClass schoolClass) {
        if (schoolClass == null) {
            return null;
        }
        return classGuardianRepository.findFirstBySchoolClassIdAndAcademicYearActiveTrue(schoolClass.getId())
                .map(guardian -> guardian.getPengurus().getUser())
                .orElse(null);
    }

    private User activeRoomGuardianUser(Room room) {
        if (room == null) {
            return null;
        }
        return roomGuardianRepository.findFirstByRoomIdAndAcademicYearActiveTrue(room.getId())
                .map(guardian -> guardian.getPengurus().getUser())
                .orElse(null);
    }

    private boolean isActiveClassGuardian(SchoolClass schoolClass, User user) {
        User guardianUser = activeClassGuardianUser(schoolClass);
        return guardianUser != null && guardianUser.getId().equals(user.getId());
    }

    private boolean isActiveRoomGuardian(Room room, User user) {
        User guardianUser = activeRoomGuardianUser(room);
        return guardianUser != null && guardianUser.getId().equals(user.getId());
    }
}
