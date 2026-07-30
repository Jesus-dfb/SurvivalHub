package com.survivalhub.repository;

import com.survivalhub.model.TaskResource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskResourceRepository extends JpaRepository<TaskResource, Long> {

    List<TaskResource> findByTaskIdOrderBySortOrderAscIdAsc(Long taskId);

    Optional<TaskResource> findByTaskIdAndId(Long taskId, Long id);

    long countByTaskId(Long taskId);

    void deleteByTaskId(Long taskId);
}
