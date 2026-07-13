package com.survivalhub.repository;

import com.survivalhub.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByWorldId(Long worldId);

    Optional<Task> findByWorldIdAndId(Long worldId, Long id);

    void deleteByWorldId(Long worldId);
}
