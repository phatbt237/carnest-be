package com.example.carnest.Repository;

import com.example.carnest.Entity.Showcase;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ShowcaseRepository extends JpaRepository<Showcase, Long> {
    List<Showcase> findByUserIdOrderBySortOrderAsc(Long userId);

    @Query("SELECT s FROM Showcase s WHERE s.isPublic = true ORDER BY s.id DESC")
    List<Showcase> findPublicNewest(Pageable pageable);

    @Query("SELECT s FROM Showcase s WHERE s.isPublic = true AND s.id < :cursor ORDER BY s.id DESC")
    List<Showcase> findPublicNewestAfterCursor(@Param("cursor") Long cursor, Pageable pageable);

    @Query("SELECT s FROM Showcase s WHERE s.isPublic = true ORDER BY s.likeCount DESC, s.id DESC")
    List<Showcase> findPublicPopular(Pageable pageable);

    @Query("SELECT s FROM Showcase s WHERE s.isPublic = true AND (s.likeCount < :cursorLike OR (s.likeCount = :cursorLike AND s.id < :cursorId)) ORDER BY s.likeCount DESC, s.id DESC")
    List<Showcase> findPublicPopularAfterCursor(@Param("cursorLike") Integer cursorLike, @Param("cursorId") Long cursorId, Pageable pageable);

    @Query("SELECT s FROM Showcase s WHERE s.isPublic = true ORDER BY s.viewCount DESC, s.id DESC")
    List<Showcase> findPublicMostViewed(Pageable pageable);

    @Query("SELECT s FROM Showcase s WHERE s.isPublic = true AND (s.viewCount < :cursorView OR (s.viewCount = :cursorView AND s.id < :cursorId)) ORDER BY s.viewCount DESC, s.id DESC")
    List<Showcase> findPublicMostViewedAfterCursor(@Param("cursorView") Integer cursorView, @Param("cursorId") Long cursorId, Pageable pageable);

    @Query("SELECT COUNT(s) FROM Showcase s WHERE s.isPublic = true")
    Long countPublic();
}
