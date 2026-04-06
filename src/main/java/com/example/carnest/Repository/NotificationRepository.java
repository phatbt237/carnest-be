package com.example.carnest.Repository;

import com.example.carnest.Entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId " +
            "AND (:cursorId IS NULL OR n.id < :cursorId) ORDER BY n.id DESC")
    List<Notification> findByUserId(@Param("userId") Long userId,
                                    @Param("cursorId") Long cursorId, @Param("limit") int limit);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user.id = :userId AND n.isRead = false")
    Long countUnread(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user.id = :userId AND n.isRead = false")
    void markAllRead(@Param("userId") Long userId);

    Long countByUserId(Long userId);
}