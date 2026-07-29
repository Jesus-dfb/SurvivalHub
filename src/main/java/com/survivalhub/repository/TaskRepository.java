package com.survivalhub.repository;

import com.survivalhub.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByWorldId(Long worldId);

    List<Task> findByWorldIdOrderBySortOrderAscIdAsc(Long worldId);

    Optional<Task> findByWorldIdAndId(Long worldId, Long id);

    boolean existsByWorldIdAndTitleIgnoreCase(Long worldId, String title);

    long countByWorldId(Long worldId);

    void deleteByWorldId(Long worldId);
}
