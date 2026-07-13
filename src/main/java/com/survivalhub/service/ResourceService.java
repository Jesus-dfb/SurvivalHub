package com.survivalhub.service;

import com.survivalhub.model.TaskResource;
import com.survivalhub.model.TaskProgressSummary;
import com.survivalhub.repository.TaskResourceRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ResourceService {

    private final TaskResourceRepository taskResourceRepository;

    public ResourceService(TaskResourceRepository taskResourceRepository) {
        this.taskResourceRepository = taskResourceRepository;
    }

    public List<TaskResource> getResourcesByTaskId(Long taskId) {
        return taskResourceRepository.findByTaskIdOrderByIdAsc(taskId);
    }

    public TaskProgressSummary getTaskProgressSummary(Long taskId) {
        List<TaskResource> taskResources = getResourcesByTaskId(taskId);

        int totalResources = taskResources.size();
        int completedResources = 0;
        int totalRequiredQuantity = 0;
        int totalCollectedQuantity = 0;

        for (TaskResource resource : taskResources) {
            if (resource.isCompleted()) {
                completedResources++;
            }

            totalRequiredQuantity += resource.getRequiredQuantity();
            totalCollectedQuantity += Math.min(
                    resource.getCollectedQuantity(),
                    resource.getRequiredQuantity()
            );
        }

        double progressPercentage = 0;

        if (totalRequiredQuantity > 0) {
            progressPercentage = totalCollectedQuantity * 100.0 / totalRequiredQuantity;
            progressPercentage = Math.round(progressPercentage * 100.0) / 100.0;
        }

        boolean allResourcesCompleted = totalResources > 0 && completedResources == totalResources;

        return new TaskProgressSummary(
                taskId,
                totalResources,
                completedResources,
                progressPercentage,
                allResourcesCompleted
        );
    }

    public Optional<TaskResource> getResourceById(Long taskId, Long resourceId) {
        return taskResourceRepository.findByTaskIdAndId(taskId, resourceId);
    }

    public TaskResource createResource(Long taskId, TaskResource resource) {
        resource.setId(null);
        resource.setTaskId(taskId);

        return taskResourceRepository.save(resource);
    }

    public Optional<TaskResource> updateResource(Long taskId, Long resourceId, TaskResource updatedResource) {
        Optional<TaskResource> resourceOptional = getResourceById(taskId, resourceId);

        if (resourceOptional.isEmpty()) {
            return Optional.empty();
        }

        TaskResource resource = resourceOptional.get();
        resource.setName(updatedResource.getName());
        resource.setRequiredQuantity(updatedResource.getRequiredQuantity());
        resource.setCollectedQuantity(updatedResource.getCollectedQuantity());

        return Optional.of(taskResourceRepository.save(resource));
    }

    public boolean deleteResource(Long taskId, Long resourceId) {
        Optional<TaskResource> resourceOptional = getResourceById(taskId, resourceId);

        if (resourceOptional.isEmpty()) {
            return false;
        }

        taskResourceRepository.delete(resourceOptional.get());

        return true;
    }

    @Transactional
    public void deleteResourcesByTaskId(Long taskId) {
        taskResourceRepository.deleteByTaskId(taskId);
    }
}
