package com.pomosda.permission.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("/api/reports/permissions")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','KAUR_ASRAMA')")
public class ReportController {
    private static final ZoneId JAKARTA_ZONE = ZoneId.of("Asia/Jakarta");
    private static final DateTimeFormatter CSV_DATE_TIME = DateTimeFormatter
            .ofPattern("dd MMMM yyyy HH:mm 'WIB'", Locale.forLanguageTag("id-ID"))
            .withZone(JAKARTA_ZONE);

    private final PermissionRequestRepository repository;

    @GetMapping
    @Transactional(readOnly = true)
    List<PermissionDto> report(@RequestParam(required = false) PermissionStatus status,
                               @RequestParam(required = false) UUID academicYearId,
                               @RequestParam(required = false) UUID classId,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end) {
        return filtered(status, academicYearId, classId, start, end).stream()
                .map(PermissionDto::from)
                .toList();
    }

    @GetMapping("/export-csv")
    @Transactional(readOnly = true)
    ResponseEntity<String> exportCsv(@RequestParam(required = false) PermissionStatus status,
                                     @RequestParam(required = false) UUID academicYearId,
                                     @RequestParam(required = false) UUID classId,
                                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
                                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end) {
        StringBuilder csv = new StringBuilder("\uFEFF");
        csv.append("Santri;Kelas;Kamar;Wali Santri;Jenis Izin;Alasan;Tujuan;Mulai;Estimasi Kembali;Check-out;Check-in;Status\n");
        filtered(status, academicYearId, classId, start, end).forEach(p -> csv
                .append(escape(p.getStudent().getName())).append(';')
                .append(escape(p.getStudent().getSchoolClass() == null ? "-" : p.getStudent().getSchoolClass().getName())).append(';')
                .append(escape(p.getStudent().getRoom() == null ? "-" : p.getStudent().getRoom().getName())).append(';')
                .append(escape(p.getStudent().getParentGuardian() == null ? "-" : p.getStudent().getParentGuardian().getName())).append(';')
                .append(escape(p.getPermissionType())).append(';')
                .append(escape(p.getReason())).append(';')
                .append(escape(p.getDestination())).append(';')
                .append(escape(formatInstant(p.getStartAt()))).append(';')
                .append(escape(formatInstant(p.getExpectedReturnAt()))).append(';')
                .append(escape(formatInstant(p.getCheckedOutAt()))).append(';')
                .append(escape(formatInstant(p.getCheckedInAt()))).append(';')
                .append(escape(statusLabel(p.getStatus()))).append('\n'));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=laporan-izin.csv")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv.toString());
    }

    private String escape(String value) {
        String safeValue = value == null || value.isBlank() ? "-" : value;
        return "\"" + safeValue.replace("\"", "\"\"") + "\"";
    }

    private List<PermissionRequest> filtered(PermissionStatus status, UUID academicYearId, UUID classId, Instant start, Instant end) {
        return repository.findAll().stream()
                .filter(p -> status == null || p.getStatus() == status)
                .filter(p -> academicYearId == null || matchesAcademicYear(p, academicYearId))
                .filter(p -> classId == null || (p.getStudent().getSchoolClass() != null && p.getStudent().getSchoolClass().getId().equals(classId)))
                .filter(p -> start == null || !p.getStartAt().isBefore(start))
                .filter(p -> end == null || !p.getStartAt().isAfter(end))
                .toList();
    }

    private boolean matchesAcademicYear(PermissionRequest permission, UUID academicYearId) {
        if (permission.getSemester() != null && permission.getSemester().getAcademicYear().getId().equals(academicYearId)) {
            return true;
        }
        return permission.getStudent().getSchoolClass() != null
                && permission.getStudent().getSchoolClass().getAcademicYear() != null
                && permission.getStudent().getSchoolClass().getAcademicYear().getId().equals(academicYearId);
    }

    private String formatInstant(Instant instant) {
        return instant == null ? "-" : CSV_DATE_TIME.format(instant);
    }

    private String statusLabel(PermissionStatus status) {
        return switch (status) {
            case DRAFT -> "Draft";
            case PENDING_WALI_KELAS -> "Menunggu Wali Kelas";
            case REJECTED_BY_WALI_KELAS -> "Ditolak Wali Kelas";
            case PENDING_WALI_KAMAR -> "Menunggu Wali Kamar";
            case REJECTED_BY_WALI_KAMAR -> "Ditolak Wali Kamar";
            case APPROVED -> "Disetujui";
            case CHECKED_OUT -> "Sudah Keluar";
            case CHECKED_IN -> "Sudah Kembali";
            case COMPLETED -> "Selesai";
            case OVERDUE -> "Terlambat";
            case CANCELLED -> "Dibatalkan";
        };
    }
}
