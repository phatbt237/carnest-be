package com.example.carnest.Repository;

import com.example.carnest.Entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    @Query("SELECT c FROM Conversation c WHERE " +
            "(c.user1.id = :u1 AND c.user2.id = :u2) OR (c.user1.id = :u2 AND c.user2.id = :u1)")
    Optional<Conversation> findByUsers(@Param("u1") Long u1, @Param("u2") Long u2);

    @Query("SELECT c FROM Conversation c " +
            "JOIN FETCH c.user1 JOIN FETCH c.user2 " +
            "WHERE (c.user1.id = :userId OR c.user2.id = :userId) " +
            "AND (:cursorId IS NULL OR c.id < :cursorId) " +
            "ORDER BY c.lastMessageAt DESC NULLS LAST, c.id DESC")
    List<Conversation> findByUserId(@Param("userId") Long userId,
                                    @Param("cursorId") Long cursorId, @Param("limit") int limit);

    @Query("SELECT COUNT(c) FROM Conversation c WHERE c.user1.id = :userId OR c.user2.id = :userId")
    Long countByUserId(@Param("userId") Long userId);
}