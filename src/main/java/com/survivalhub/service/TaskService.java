package com.survivalhub.service;

import com.survivalhub.model.Task;
import com.survivalhub.repository.TaskRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<Task> getTasksByWorldId(Long worldId) {
        return taskRepository.findByWorldId(worldId);
    }

    public Optional<Task> getTaskById(Long worldId, Long taskId) {
        return taskRepository.findByWorldIdAndId(worldId, taskId);
    }

    public Task createTask(Long worldId, Task task) {
        task.setId(null);
        task.setWorldId(worldId);

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
        task.setDueDate(updatedTask.getDueDate());
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

    @Transactional
    public void deleteTasksByWorldId(Long worldId) {
        taskRepository.deleteByWorldId(worldId);
    }
}
