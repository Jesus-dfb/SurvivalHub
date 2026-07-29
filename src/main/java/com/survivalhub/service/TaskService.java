package com.survivalhub.service;

import com.survivalhub.model.Task;
import com.survivalhub.repository.TaskRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<Task> getTasksByWorldId(Long worldId) {
        List<Task> tasks = taskRepository.findByWorldIdOrderBySortOrderAscIdAsc(worldId);

        return normalizeTaskOrder(tasks);
    }

    public Optional<Task> getTaskById(Long worldId, Long taskId) {
        return taskRepository.findByWorldIdAndId(worldId, taskId);
    }

    public boolean existsByWorldIdAndTitle(Long worldId, String title) {
        if (title == null || title.isBlank()) {
            return false;
        }

        return taskRepository.existsByWorldIdAndTitleIgnoreCase(worldId, title.trim());
    }

    public Task createTask(Long worldId, Task task) {
        task.setId(null);
        task.setWorldId(worldId);

        if (task.getSortOrder() <= 0) {
            task.setSortOrder((int) taskRepository.countByWorldId(worldId) + 1);
        }

        return taskRepository.save(task);
    }

    public Optional<Task> updateTask(Long worldId, Long taskId, Task updatedTask) {
        Optional<Task> taskOptional = getTaskById(worldId, taskId);

        if (taskOptional.isEmpty()) {
            return Optional.empty();
        }

        Task task = taskOptional.get();
        task.setTitle(updatedTask.getTitle());
        task.setDescription(updatedTask.getDescription());
        task.setPriority(updatedTask.getPriority());
        task.setCompleted(updatedTask.isCompleted());

        return Optional.of(taskRepository.save(task));
    }

    public boolean deleteTask(Long worldId, Long taskId) {
        Optional<Task> taskOptional = getTaskById(worldId, taskId);

        if (taskOptional.isEmpty()) {
            return false;
        }

        taskRepository.delete(taskOptional.get());

        return true;
    }

    public List<Task> reorderTasks(Long worldId, List<Long> taskIds) {
        List<Task> tasks = getTasksByWorldId(worldId);
        Map<Long, Task> taskById = new HashMap<>();

        for (Task task : tasks) {
            taskById.put(task.getId(), task);
        }

        int sortOrder = 1;

        for (Long taskId : taskIds) {
            Task task = taskById.remove(taskId);

            if (task != null) {
                task.setSortOrder(sortOrder);
                sortOrder++;
            }
        }

        for (Task task : tasks) {
            if (taskById.containsKey(task.getId())) {
                task.setSortOrder(sortOrder);
                sortOrder++;
            }
        }

        taskRepository.saveAll(tasks);

        return getTasksByWorldId(worldId);
    }

    @Transactional
    public void deleteTasksByWorldId(Long worldId) {
        taskRepository.deleteByWorldId(worldId);
    }

    private List<Task> normalizeTaskOrder(List<Task> tasks) {
        boolean needsUpdate = false;
        int sortOrder = 1;

        for (Task task : tasks) {
            if (task.getSortOrder() <= 0) {
                task.setSortOrder(sortOrder);
                needsUpdate = true;
            }

            sortOrder++;
        }

        if (needsUpdate) {
            return taskRepository.saveAll(tasks);
        }

        return tasks;
    }
}
