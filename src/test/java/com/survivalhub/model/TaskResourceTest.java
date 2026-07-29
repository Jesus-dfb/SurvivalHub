package com.survivalhub.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TaskResourceTest {

    @Test
    void resourceIsCompletedWhenCollectedQuantityReachesRequiredQuantity() {
        TaskResource pendingResource = new TaskResource(null, 1L, "Piedra", 64, 40);
        TaskResource completedResource = new TaskResource(null, 1L, "Camas", 10, 10);
        TaskResource overCompletedResource = new TaskResource(null, 1L, "Hierro", 30, 35);

        assertThat(pendingResource.isCompleted()).isFalse();
        assertThat(completedResource.isCompleted()).isTrue();
        assertThat(overCompletedResource.isCompleted()).isTrue();
    }
}
