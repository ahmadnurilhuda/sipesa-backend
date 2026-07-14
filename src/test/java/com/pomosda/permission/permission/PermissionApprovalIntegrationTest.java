package com.pomosda.permission.permission;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PermissionApprovalIntegrationTest {
    @Test
    void placeholderForApproveRejectIntegration() {
        assertThat(PermissionStatus.REJECTED_BY_WALI_KELAS.name()).contains("WALI_KELAS");
    }
}
