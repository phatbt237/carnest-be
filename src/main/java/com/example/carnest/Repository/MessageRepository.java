package com.example.carnest.Repository;

import com.example.carnest.Entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("SELECT m FROM Message m JOIN FETCH m.sender " +
            "WHERE m.conversation.id = :convId " +
            "AND (:cursorId IS NULL OR m.id < :cursorId) " +
            "ORDER BY m.id DESC")
    List<Message> findByConversationId(@Param("convId") Long convId,
                                       @Param("cursorId") Long cursorId, @Param("limit") int limit);

    Long countByConversationId(Long convId);
}