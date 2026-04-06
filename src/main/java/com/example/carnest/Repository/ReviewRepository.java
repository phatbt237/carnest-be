package com.example.carnest.Repository;

import com.example.carnest.Entity.Review;
import com.example.carnest.Enum.ReviewType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Boolean existsByOrderIdAndReviewerId(Long orderId, Long reviewerId);

    @Query("SELECT r FROM Review r JOIN FETCH r.reviewer JOIN FETCH r.reviewed " +
            "WHERE r.id = :id")
    Optional<Review> findByIdFull(@Param("id") Long id);

    // Review của 1 user (được đánh giá)
    @Query("SELECT r FROM Review r JOIN FETCH r.reviewer " +
            "WHERE r.reviewed.id = :userId AND r.type = :type AND r.isHidden = false " +
            "AND (:cursorId IS NULL OR r.id < :cursorId) ORDER BY r.id DESC")
    List<Review> findByReviewedId(@Param("userId") Long userId, @Param("type") ReviewType type,
                                  @Param("cursorId") Long cursorId, @Param("limit") int limit);

    // Review theo shop (seller)
    @Query("SELECT r FROM Review r JOIN FETCH r.reviewer " +
            "WHERE r.reviewed.id = :sellerId AND r.type = 'BUYER_TO_SELLER' AND r.isHidden = false " +
            "AND (:cursorId IS NULL OR r.id < :cursorId) ORDER BY r.id DESC")
    List<Review> findByShopSellerId(@Param("sellerId") Long sellerId,
                                    @Param("cursorId") Long cursorId, @Param("limit") int limit);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.reviewed.id = :userId AND r.type = :type AND r.isHidden = false")
    Long countByReviewedId(@Param("userId") Long userId, @Param("type") ReviewType type);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.reviewed.id = :userId AND r.type = :type AND r.isHidden = false")
    Double avgRatingByReviewedId(@Param("userId") Long userId, @Param("type") ReviewType type);
}