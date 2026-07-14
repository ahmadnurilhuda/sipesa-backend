package com.pomosda.permission.permission;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class QrScanIntegrationTest {
    @Test
    void placeholderForQrScanIntegration() {
        assertThat("/api/security/scan/check-out").contains("scan");
    }
}
