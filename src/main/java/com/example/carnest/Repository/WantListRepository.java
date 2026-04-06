package com.example.carnest.Repository;

import com.example.carnest.Entity.WantList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WantListRepository extends JpaRepository<WantList, Long> {

    @Query("SELECT w FROM WantList w WHERE w.user.id = :userId " +
            "AND (:cursorId IS NULL OR w.id < :cursorId) ORDER BY w.id DESC")
    List<WantList> findByUserId(@Param("userId") Long userId,
                                @Param("cursorId") Long cursorId, @Param("limit") int limit);

    @Query("SELECT w FROM WantList w WHERE w.isPublic = true AND w.status = 'ACTIVE' " +
            "AND (:cursorId IS NULL OR w.id < :cursorId) ORDER BY w.id DESC")
    List<WantList> findPublicActive(@Param("cursorId") Long cursorId, @Param("limit") int limit);

    Long countByUserId(Long userId);
}