package com.survivalhub.repository;

import com.survivalhub.model.GuideFavorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface GuideFavoriteRepository extends JpaRepository<GuideFavorite, Long> {

    List<GuideFavorite> findByUserId(Long userId);

    Optional<GuideFavorite> findByGuideIdAndUserId(Long guideId, Long userId);

    boolean existsByGuideIdAndUserId(Long guideId, Long userId);

    @Transactional
    void deleteByGuideIdAndUserId(Long guideId, Long userId);
}
