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

    @Test
    void requiredStackSummarySeparatesFullStacksAndLooseItems() {
        TaskResource resource = new TaskResource(null, 1L, "Tronco de abeto", 657, 0);
        resource.setStackSize(64);

        assertThat(resource.getRequiredStacks()).isEqualTo(10);
        assertThat(resource.getRequiredLooseItems()).isEqualTo(17);
        assertThat(resource.getRequiredStackSummary()).isEqualTo("10 stacks y 17 bloques");
    }

    @Test
    void requiredStackSummaryCanShowOnlyLooseItems() {
        TaskResource resource = new TaskResource(null, 1L, "Faro", 1, 0);
        resource.setStackSize(64);

        assertThat(resource.getRequiredStacks()).isZero();
        assertThat(resource.getRequiredLooseItems()).isEqualTo(1);
        assertThat(resource.getRequiredStackSummary()).isEqualTo("0");
    }

    @Test
    void requiredStackSummaryCanShowOnlyFullStacks() {
        TaskResource resource = new TaskResource(null, 1L, "Piedra", 128, 0);
        resource.setStackSize(64);

        assertThat(resource.getRequiredStacks()).isEqualTo(2);
        assertThat(resource.getRequiredLooseItems()).isZero();
        assertThat(resource.getRequiredStackSummary()).isEqualTo("2 stacks");
    }

    @Test
    void remainingQuantityShowsMissingAmount() {
        TaskResource resource = new TaskResource(null, 1L, "Piedra", 64, 40);

        assertThat(resource.getRemainingQuantity()).isEqualTo(24);
    }

    @Test
    void remainingQuantityDoesNotGoBelowZero() {
        TaskResource resource = new TaskResource(null, 1L, "Andamio", 680, 690);

        assertThat(resource.getRemainingQuantity()).isZero();
    }
}
