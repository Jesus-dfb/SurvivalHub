package com.survivalhub.model;

public class WorldDashboardSummary {

    private Long worldId;
    private int memberCount;
    private int taskCount;
    private int completedTaskCount;
    private int pendingTaskCount;
    private int resourceCount;
    private int completedResourceCount;
    private double averageTaskProgress;

    public WorldDashboardSummary() {
    }

    public WorldDashboardSummary(
            Long worldId,
            int memberCount,
            int taskCount,
            int completedTaskCount,
            int pendingTaskCount,
            int resourceCount,
            int completedResourceCount,
            double averageTaskProgress
    ) {
        this.worldId = worldId;
        this.memberCount = memberCount;
        this.taskCount = taskCount;
        this.completedTaskCount = completedTaskCount;
        this.pendingTaskCount = pendingTaskCount;
        this.resourceCount = resourceCount;
        this.completedResourceCount = completedResourceCount;
        this.averageTaskProgress = averageTaskProgress;
    }

    public Long getWorldId() {
        return worldId;
    }

    public void setWorldId(Long worldId) {
        this.worldId = worldId;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public int getTaskCount() {
        return taskCount;
    }

    public void setTaskCount(int taskCount) {
        this.taskCount = taskCount;
    }

    public int getCompletedTaskCount() {
        return completedTaskCount;
    }

    public void setCompletedTaskCount(int completedTaskCount) {
        this.completedTaskCount = completedTaskCount;
    }

    public int getPendingTaskCount() {
        return pendingTaskCount;
    }

    public void setPendingTaskCount(int pendingTaskCount) {
        this.pendingTaskCount = pendingTaskCount;
    }

    public int getResourceCount() {
        return resourceCount;
    }

    public void setResourceCount(int resourceCount) {
        this.resourceCount = resourceCount;
    }

    public int getCompletedResourceCount() {
        return completedResourceCount;
    }

    public void setCompletedResourceCount(int completedResourceCount) {
        this.completedResourceCount = completedResourceCount;
    }

    public double getAverageTaskProgress() {
        return averageTaskProgress;
    }

    public void setAverageTaskProgress(double averageTaskProgress) {
        this.averageTaskProgress = averageTaskProgress;
    }
}
