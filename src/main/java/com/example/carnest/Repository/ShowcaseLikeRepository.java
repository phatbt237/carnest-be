package com.example.carnest.Repository;

import com.example.carnest.Entity.ShowcaseLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ShowcaseLikeRepository extends JpaRepository<ShowcaseLike, Long> {
    Optional<ShowcaseLike> findByShowcaseIdAndUserId(Long showcaseId, Long userId);
    Boolean existsByShowcaseIdAndUserId(Long showcaseId, Long userId);
}