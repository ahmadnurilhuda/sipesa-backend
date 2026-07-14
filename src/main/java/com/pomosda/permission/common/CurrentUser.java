package com.pomosda.permission.common;

import com.pomosda.permission.exception.ApiException;
import com.pomosda.permission.user.User;
import com.pomosda.permission.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurrentUser {
    private final UserRepository userRepository;

    public User get(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Sesi tidak valid"));
    }
}
