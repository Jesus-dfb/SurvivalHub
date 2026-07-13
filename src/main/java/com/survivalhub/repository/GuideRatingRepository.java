package com.survivalhub.repository;

import com.survivalhub.model.GuideRating;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GuideRatingRepository extends JpaRepository<GuideRating, Long> {

    List<GuideRating> findByGuideId(Long guideId);

    List<GuideRating> findByUserNameIgnoreCase(String userName);

    Optional<GuideRating> findByGuideIdAndUserNameIgnoreCase(Long guideId, String userName);
}
