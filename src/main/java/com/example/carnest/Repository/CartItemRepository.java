package com.example.carnest.Repository;

import com.example.carnest.Entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    @Query("SELECT ci FROM CartItem ci " +
           "JOIN FETCH ci.product p " +
           "JOIN FETCH p.shop s " +
           "LEFT JOIN FETCH p.brand " +
           "WHERE ci.user.id = :userId " +
           "ORDER BY ci.addedAt DESC")
    List<CartItem> findByUserIdWithProduct(@Param("userId") Long userId);

    Optional<CartItem> findByUserIdAndProductId(Long userId, Long productId);

    Boolean existsByUserIdAndProductId(Long userId, Long productId);

    void deleteByUserId(Long userId);

    void deleteByUserIdAndProductId(Long userId, Long productId);

    @Query("SELECT COUNT(ci) FROM CartItem ci WHERE ci.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);
}
