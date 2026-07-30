package com.survivalhub.model;

import java.util.ArrayList;
import java.util.List;

public class ResourceImportResult {

    private int importedCount;
    private int createdCount;
    private int updatedCount;
    private int skippedCount;
    private List<TaskResource> resources = new ArrayList<>();

    public ResourceImportResult() {
    }

    public ResourceImportResult(
            int importedCount,
            int createdCount,
            int updatedCount,
            int skippedCount,
            List<TaskResource> resources
    ) {
        this.importedCount = importedCount;
        this.createdCount = createdCount;
        this.updatedCount = updatedCount;
        this.skippedCount = skippedCount;
        this.resources = resources;
    }

    public int getImportedCount() {
        return importedCount;
    }

    public void setImportedCount(int importedCount) {
        this.importedCount = importedCount;
    }

    public int getCreatedCount() {
        return createdCount;
    }

    public void setCreatedCount(int createdCount) {
        this.createdCount = createdCount;
    }

    public int getUpdatedCount() {
        return updatedCount;
    }

    public void setUpdatedCount(int updatedCount) {
        this.updatedCount = updatedCount;
    }

    public int getSkippedCount() {
        return skippedCount;
    }

    public void setSkippedCount(int skippedCount) {
        this.skippedCount = skippedCount;
    }

    public List<TaskResource> getResources() {
        return resources;
    }

    public void setResources(List<TaskResource> resources) {
        this.resources = resources;
    }
}
