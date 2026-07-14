package com.pomosda.permission.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UserDto(UUID id, String name, String username, String email, String phone, Role role, boolean active) {
    public static UserDto from(User user) {
        return new UserDto(user.getId(), user.getName(), user.getUsername(), user.getEmail(), user.getPhone(), user.getRole(), user.isActive());
    }
}

record UserRequest(
        @NotBlank String name,
        @NotBlank String username,
        @NotBlank @Email String email,
        String phone,
        String password,
        @NotNull Role role,
        boolean active
) {
}
