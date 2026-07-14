package com.pomosda.permission.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService service;

    @GetMapping
    List<NotificationDto> mine(Authentication authentication) {
        return service.mine(authentication);
    }

    @PutMapping("/{id}/read")
    NotificationDto read(@PathVariable UUID id, Authentication authentication) {
        return service.read(id, authentication);
    }
}
