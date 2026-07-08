package com.example.carnest.Repository;

import com.example.carnest.Entity.WantListContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WantListContactRepository extends JpaRepository<WantListContact, Long> {

    @Modifying
    @Query(value = "INSERT IGNORE INTO want_list_contact (want_list_id, user_id, created_at) " +
            "VALUES (:wantListId, :userId, NOW())", nativeQuery = true)
    int insertIfAbsent(@Param("wantListId") Long wantListId, @Param("userId") Long userId);
}
