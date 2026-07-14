package com.pomosda.permission.auth;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuthEndpointIntegrationTest {
    @Test
    void placeholderForLoginIntegration() {
        assertThat("/api/auth/login").startsWith("/api/auth");
    }
}
