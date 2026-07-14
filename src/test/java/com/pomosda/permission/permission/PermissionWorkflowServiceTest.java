package com.pomosda.permission.permission;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PermissionWorkflowServiceTest {
    @Test
    void statusOrderDocumentsMainWorkflow() {
        assertThat(PermissionStatus.PENDING_WALI_KELAS).isNotNull();
        assertThat(PermissionStatus.PENDING_WALI_KAMAR).isNotNull();
        assertThat(PermissionStatus.APPROVED).isNotNull();
        assertThat(PermissionStatus.CHECKED_OUT).isNotNull();
        assertThat(PermissionStatus.CHECKED_IN).isNotNull();
        assertThat(PermissionStatus.COMPLETED).isNotNull();
    }
}
