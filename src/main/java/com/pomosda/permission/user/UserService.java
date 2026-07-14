package com.pomosda.permission.user;

import com.pomosda.permission.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public List<UserDto> all() {
        return repository.findAll().stream().map(UserDto::from).toList();
    }

    public UserDto get(UUID id) {
        return UserDto.from(entity(id));
    }

    public UserDto create(UserRequest request) {
        if (repository.existsByUsernameOrEmail(request.username(), request.email())) {
            throw new ApiException(HttpStatus.CONFLICT, "Username atau email sudah dipakai");
        }
        validatePhone(request.phone(), null);
        User user = new User();
        apply(user, request);
        user.setPasswordHash(passwordEncoder.encode(request.password() == null ? "password" : request.password()));
        return UserDto.from(repository.save(user));
    }

    public UserDto update(UUID id, UserRequest request) {
        User user = entity(id);
        if (repository.existsByUsernameAndIdNot(request.username(), id)) {
            throw new ApiException(HttpStatus.CONFLICT, "Username sudah dipakai");
        }
        if (repository.existsByEmailAndIdNot(request.email(), id)) {
            throw new ApiException(HttpStatus.CONFLICT, "Email sudah dipakai");
        }
        validatePhone(request.phone(), id);
        apply(user, request);
        if (request.password() != null && !request.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }
        return UserDto.from(repository.save(user));
    }

    public void delete(UUID id) {
        repository.delete(entity(id));
    }

    private void apply(User user, UserRequest request) {
        user.setName(request.name());
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPhone(request.phone());
        user.setRole(request.role());
        user.setActive(request.active());
    }

    private void validatePhone(String phone, UUID currentUserId) {
        if (phone == null || phone.isBlank()) {
            return;
        }
        boolean used = currentUserId == null
                ? repository.existsByPhone(phone)
                : repository.existsByPhoneAndIdNot(phone, currentUserId);
        if (used) {
            throw new ApiException(HttpStatus.CONFLICT, "No. HP sudah dipakai");
        }
    }

    private User entity(UUID id) {
        return repository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User tidak ditemukan"));
    }
}
