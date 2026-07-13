package com.survivalhub.service;

import com.survivalhub.model.Member;
import com.survivalhub.model.Task;
import com.survivalhub.model.TaskProgressSummary;
import com.survivalhub.model.TaskResource;
import com.survivalhub.model.WorldDashboardSummary;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardService {

    private final MemberService memberService;
    private final TaskService taskService;
    private final ResourceService resourceService;

    public DashboardService(
            MemberService memberService,
            TaskService taskService,
            ResourceService resourceService
    ) {
        this.memberService = memberService;
        this.taskService = taskService;
        this.resourceService = resourceService;
    }

    public WorldDashboardSummary getWorldDashboard(Long worldId) {
        List<Member> members = memberService.getMembersByWorldId(worldId);
        List<Task> tasks = taskService.getTasksByWorldId(worldId);

        int completedTaskCount = 0;
        int resourceCount = 0;
        int completedResourceCount = 0;
        double progressTotal = 0;

        for (Task task : tasks) {
            if (task.isCompleted()) {
                completedTaskCount++;
            }

            List<TaskResource> resources = resourceService.getResourcesByTaskId(task.getId());
            resourceCount += resources.size();

            for (TaskResource resource : resources) {
                if (resource.isCompleted()) {
                    completedResourceCount++;
                }
            }

            TaskProgressSummary taskSummary = resourceService.getTaskProgressSummary(task.getId());
            progressTotal += taskSummary.getProgressPercentage();
        }

        int pendingTaskCount = tasks.size() - completedTaskCount;
        double averageTaskProgress = 0;

        if (!tasks.isEmpty()) {
            averageTaskProgress = progressTotal / tasks.size();
            averageTaskProgress = Math.round(averageTaskProgress * 100.0) / 100.0;
        }

        return new WorldDashboardSummary(
                worldId,
                members.size(),
                tasks.size(),
                completedTaskCount,
                pendingTaskCount,
                resourceCount,
                completedResourceCount,
                averageTaskProgress
        );
    }
}
