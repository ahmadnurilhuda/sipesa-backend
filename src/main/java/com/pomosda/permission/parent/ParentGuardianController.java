package com.pomosda.permission.parent;

import com.pomosda.permission.exception.ApiException;
import com.pomosda.permission.user.Role;
import com.pomosda.permission.user.User;
import com.pomosda.permission.user.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/parents")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ParentGuardianController {
    private final ParentGuardianRepository repository;
    private final UserRepository userRepository;

    @GetMapping
    @Transactional(readOnly = true)
    List<ParentGuardianDto> all() {
        return repository.findAll().stream().map(ParentGuardianDto::from).toList();
    }

    @PostMapping
    @Transactional
    ParentGuardianDto create(@Valid @RequestBody ParentGuardianRequest request) {
        ParentGuardian parent = new ParentGuardian();
        apply(parent, request);
        return ParentGuardianDto.from(repository.save(parent));
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    ParentGuardianDto get(@PathVariable UUID id) {
        return ParentGuardianDto.from(entity(id));
    }

    @PutMapping("/{id}")
    @Transactional
    ParentGuardianDto update(@PathVariable UUID id, @Valid @RequestBody ParentGuardianRequest request) {
        ParentGuardian parent = entity(id);
        apply(parent, request);
        return ParentGuardianDto.from(repository.save(parent));
    }

    @DeleteMapping("/{id}")
    void delete(@PathVariable UUID id) {
        repository.delete(entity(id));
    }

    private void apply(ParentGuardian parent, ParentGuardianRequest request) {
        parent.setName(request.name());
        parent.setPhone(request.phone());
        parent.setAddress(request.address());
        parent.setUser(findWaliSantriUser(request.userId(), parent.getId()));
        validatePhone(request.phone(), parent);
        if (parent.getUser() != null && request.phone() != null && !request.phone().isBlank()) {
            parent.getUser().setPhone(request.phone());
        }
    }

    private User findWaliSantriUser(UUID userId, UUID parentId) {
        if (userId == null) {
            return null;
        }
        boolean usedByAnotherParent = parentId == null
                ? repository.existsByUserId(userId)
                : repository.existsByUserIdAndIdNot(userId, parentId);
        if (usedByAnotherParent) {
            throw new ApiException(HttpStatus.CONFLICT, "Akun login sudah dipakai oleh wali santri lain");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Akun login tidak ditemukan"));
        if (user.getRole() != Role.WALI_SANTRI) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Akun login wali santri harus memiliki role WALI_SANTRI");
        }
        return user;
    }

    private void validatePhone(String phone, ParentGuardian parent) {
        if (phone == null || phone.isBlank()) {
            return;
        }
        UUID parentId = parent.getId();
        UUID linkedUserId = parent.getUser() == null ? null : parent.getUser().getId();
        boolean usedByOtherParent = parentId == null
                ? repository.existsByPhone(phone)
                : repository.existsByPhoneAndIdNot(phone, parentId);
        if (usedByOtherParent) {
            throw new ApiException(HttpStatus.CONFLICT, "No. HP wali santri sudah dipakai");
        }
        boolean usedByOtherUser = linkedUserId == null
                ? userRepository.existsByPhone(phone)
                : userRepository.existsByPhoneAndIdNot(phone, linkedUserId);
        if (usedByOtherUser) {
            throw new ApiException(HttpStatus.CONFLICT, "No. HP sudah dipakai oleh akun lain");
        }
    }

    private ParentGuardian entity(UUID id) {
        return repository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Wali santri tidak ditemukan"));
    }
}
