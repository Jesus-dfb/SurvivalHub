package com.survivalhub.model;

import java.util.ArrayList;
import java.util.List;

public class GuideApplicationResult {

    private Guide guide;
    private Task task;
    private List<TaskResource> resources = new ArrayList<>();

    public GuideApplicationResult() {
    }

    public GuideApplicationResult(Guide guide, Task task, List<TaskResource> resources) {
        this.guide = guide;
        this.task = task;
        this.resources = resources;
    }

    public Guide getGuide() {
        return guide;
    }

    public void setGuide(Guide guide) {
        this.guide = guide;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public List<TaskResource> getResources() {
        return resources;
    }

    public void setResources(List<TaskResource> resources) {
        this.resources = resources;
    }
}
