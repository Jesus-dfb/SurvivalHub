package com.survivalhub.model;

public class TaskProgressSummary {

    private Long taskId;
    private int totalResources;
    private int completedResources;
    private double progressPercentage;
    private boolean allResourcesCompleted;

    public TaskProgressSummary() {
    }

    public TaskProgressSummary(
            Long taskId,
            int totalResources,
            int completedResources,
            double progressPercentage,
            boolean allResourcesCompleted
    ) {
        this.taskId = taskId;
        this.totalResources = totalResources;
        this.completedResources = completedResources;
        this.progressPercentage = progressPercentage;
        this.allResourcesCompleted = allResourcesCompleted;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public int getTotalResources() {
        return totalResources;
    }

    public void setTotalResources(int totalResources) {
        this.totalResources = totalResources;
    }

    public int getCompletedResources() {
        return completedResources;
    }

    public void setCompletedResources(int completedResources) {
        this.completedResources = completedResources;
    }

    public double getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(double progressPercentage) {
        this.progressPercentage = progressPercentage;
    }

    public boolean isAllResourcesCompleted() {
        return allResourcesCompleted;
    }

    public void setAllResourcesCompleted(boolean allResourcesCompleted) {
        this.allResourcesCompleted = allResourcesCompleted;
    }
}
