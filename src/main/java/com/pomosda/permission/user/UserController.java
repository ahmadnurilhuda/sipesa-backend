package com.pomosda.permission.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {
    private final UserService service;

    @GetMapping
    List<UserDto> all() {
        return service.all();
    }

    @PostMapping
    UserDto create(@Valid @RequestBody UserRequest request) {
        return service.create(request);
    }

    @GetMapping("/{id}")
    UserDto get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PutMapping("/{id}")
    UserDto update(@PathVariable UUID id, @Valid @RequestBody UserRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}
