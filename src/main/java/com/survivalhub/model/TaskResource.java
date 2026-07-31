package com.survivalhub.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "task_resources")
public class TaskResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long taskId;

    private String name;

    private int requiredQuantity;

    private int collectedQuantity;

    private Integer stackSize;

    private Integer sortOrder;

    public TaskResource() {
    }

    public TaskResource(Long id, Long taskId, String name, int requiredQuantity, int collectedQuantity) {
        this.id = id;
        this.taskId = taskId;
        this.name = name;
        this.requiredQuantity = requiredQuantity;
        this.collectedQuantity = collectedQuantity;
        this.stackSize = 64;
        this.sortOrder = 0;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRequiredQuantity() {
        return requiredQuantity;
    }

    public void setRequiredQuantity(int requiredQuantity) {
        this.requiredQuantity = requiredQuantity;
    }

    public int getCollectedQuantity() {
        return collectedQuantity;
    }

    public void setCollectedQuantity(int collectedQuantity) {
        this.collectedQuantity = collectedQuantity;
    }

    public int getStackSize() {
        return stackSize == null || stackSize <= 0 ? 64 : stackSize;
    }

    public void setStackSize(int stackSize) {
        this.stackSize = stackSize;
    }

    public int getSortOrder() {
        return sortOrder == null ? 0 : sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    @Transient
    public boolean isCompleted() {
        return collectedQuantity >= requiredQuantity;
    }

    @Transient
    public int getRemainingQuantity() {
        return Math.max(requiredQuantity - collectedQuantity, 0);
    }

    @Transient
    public int getRequiredStacks() {
        int currentStackSize = getStackSize();

        if (requiredQuantity <= 0 || currentStackSize <= 0) {
            return 0;
        }

        return requiredQuantity / currentStackSize;
    }

    @Transient
    public int getRequiredLooseItems() {
        int currentStackSize = getStackSize();

        if (requiredQuantity <= 0 || currentStackSize <= 0) {
            return 0;
        }

        return requiredQuantity % currentStackSize;
    }

    @Transient
    public String getRequiredStackSummary() {
        int stacks = getRequiredStacks();
        int looseItems = getRequiredLooseItems();

        if (stacks == 0) {
            return "0";
        }

        if (looseItems == 0) {
            return stacks == 1 ? "1 stack" : stacks + " stacks";
        }

        String stackText = stacks == 1 ? "1 stack" : stacks + " stacks";

        return stackText + " y " + looseItems + " bloques";
    }
}
