package com.survivalhub.repository;

import com.survivalhub.model.World;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorldRepository extends JpaRepository<World, Long> {

    List<World> findByOwnerUserIdOrderByIdAsc(Long ownerUserId);

    Optional<World> findByIdAndOwnerUserId(Long id, Long ownerUserId);
}
