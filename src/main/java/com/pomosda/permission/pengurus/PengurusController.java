package com.pomosda.permission.pengurus;

import com.pomosda.permission.exception.ApiException;
import com.pomosda.permission.user.User;
import com.pomosda.permission.user.UserRepository;
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
@RequestMapping("/api/pengurus")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class PengurusController {
    private final PengurusRepository repository;
    private final UserRepository userRepository;

    @GetMapping
    @Transactional(readOnly = true)
    List<PengurusDto> all() {
        return repository.findAll().stream().map(PengurusDto::from).toList();
    }

    @PostMapping
    @Transactional
    PengurusDto create(@Valid @RequestBody PengurusRequest request) {
        Pengurus pengurus = new Pengurus();
        apply(pengurus, request);
        return PengurusDto.from(repository.save(pengurus));
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    PengurusDto get(@PathVariable UUID id) {
        return PengurusDto.from(entity(id));
    }

    @PutMapping("/{id}")
    @Transactional
    PengurusDto update(@PathVariable UUID id, @Valid @RequestBody PengurusRequest request) {
        Pengurus pengurus = entity(id);
        apply(pengurus, request);
        return PengurusDto.from(repository.save(pengurus));
    }

    @DeleteMapping("/{id}")
    void delete(@PathVariable UUID id) {
        repository.delete(entity(id));
    }

    private void apply(Pengurus pengurus, PengurusRequest request) {
        pengurus.setName(request.name());
        pengurus.setNip(request.nip());
        pengurus.setPhone(request.phone());
        pengurus.setPosition(request.position());
        pengurus.setUser(findUser(request.userId()));
        validatePhone(request.phone(), pengurus);
        syncUserProfile(pengurus);
    }

    private User findUser(UUID userId) {
        if (userId == null) {
            return null;
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Akun login tidak ditemukan"));
    }

    private void syncUserProfile(Pengurus pengurus) {
        if (pengurus.getUser() == null) {
            return;
        }
        pengurus.getUser().setName(pengurus.getName());
        if (pengurus.getPhone() != null && !pengurus.getPhone().isBlank()) {
            pengurus.getUser().setPhone(pengurus.getPhone());
        }
    }

    private void validatePhone(String phone, Pengurus pengurus) {
        if (phone == null || phone.isBlank()) {
            return;
        }
        UUID pengurusId = pengurus.getId();
        UUID linkedUserId = pengurus.getUser() == null ? null : pengurus.getUser().getId();
        boolean usedByOtherPengurus = pengurusId == null
                ? repository.existsByPhone(phone)
                : repository.existsByPhoneAndIdNot(phone, pengurusId);
        if (usedByOtherPengurus) {
            throw new ApiException(HttpStatus.CONFLICT, "No. HP pengurus sudah dipakai");
        }
        boolean usedByOtherUser = linkedUserId == null
                ? userRepository.existsByPhone(phone)
                : userRepository.existsByPhoneAndIdNot(phone, linkedUserId);
        if (usedByOtherUser) {
            throw new ApiException(HttpStatus.CONFLICT, "No. HP sudah dipakai oleh akun lain");
        }
    }

    private Pengurus entity(UUID id) {
        return repository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Pengurus tidak ditemukan"));
    }
}
