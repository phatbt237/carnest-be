package com.example.carnest.Repository;

import com.example.carnest.Entity.ShopFollower;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ShopFollowerRepository extends JpaRepository<ShopFollower, Long> {

    Optional<ShopFollower> findByShopIdAndUserId(Long shopId, Long userId);

    Boolean existsByShopIdAndUserId(Long shopId, Long userId);

    // Batch check: user đang follow những shop nào trong danh sách
    @Query("SELECT sf.shop.id FROM ShopFollower sf WHERE sf.user.id = :userId AND sf.shop.id IN :shopIds")
    Set<Long> findFollowedShopIds(@Param("userId") Long userId, @Param("shopIds") List<Long> shopIds);

    // Cursor-based: danh sách shop mà user đang follow
    @Query("SELECT sf FROM ShopFollower sf JOIN FETCH sf.shop s JOIN FETCH s.user " +
           "WHERE sf.user.id = :userId " +
           "ORDER BY sf.id DESC")
    List<ShopFollower> findByUserIdWithShop(
            @Param("userId") Long userId,
            @Param("limit") int limit);

    @Query("SELECT sf FROM ShopFollower sf JOIN FETCH sf.shop s JOIN FETCH s.user " +
           "WHERE sf.user.id = :userId AND sf.id < :cursorId " +
           "ORDER BY sf.id DESC")
    List<ShopFollower> findByUserIdAfterCursor(
            @Param("userId") Long userId,
            @Param("cursorId") Long cursorId,
            @Param("limit") int limit);

    Long countByShopId(Long shopId);
    Long countByUserId(Long userId);
}
