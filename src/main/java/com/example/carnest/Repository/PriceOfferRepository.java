package com.example.carnest.Repository;

import com.example.carnest.Entity.PriceOffer;
import com.example.carnest.Enum.OfferStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PriceOfferRepository extends JpaRepository<PriceOffer, Long> {

    @Query("SELECT po FROM PriceOffer po " +
            "JOIN FETCH po.product p JOIN FETCH p.shop s " +
            "JOIN FETCH po.buyer JOIN FETCH po.seller " +
            "WHERE po.id = :id")
    Optional<PriceOffer> findByIdFull(@Param("id") Long id);

    // Offer mà buyer gửi
    @Query("SELECT po FROM PriceOffer po " +
            "JOIN FETCH po.product p " +
            "WHERE po.buyer.id = :buyerId " +
            "AND (:cursorId IS NULL OR po.id < :cursorId) " +
            "ORDER BY po.id DESC")
    List<PriceOffer> findByBuyerId(
            @Param("buyerId") Long buyerId,
            @Param("cursorId") Long cursorId,
            @Param("limit") int limit);

    // Offer mà seller nhận
    @Query("SELECT po FROM PriceOffer po " +
            "JOIN FETCH po.product p JOIN FETCH po.buyer " +
            "WHERE po.seller.id = :sellerId " +
            "AND (:cursorId IS NULL OR po.id < :cursorId) " +
            "ORDER BY po.id DESC")
    List<PriceOffer> findBySellerId(
            @Param("sellerId") Long sellerId,
            @Param("cursorId") Long cursorId,
            @Param("limit") int limit);

    // Offer cho 1 product
    @Query("SELECT po FROM PriceOffer po JOIN FETCH po.buyer " +
            "WHERE po.product.id = :productId AND po.status = :status " +
            "ORDER BY po.offerPrice DESC")
    List<PriceOffer> findByProductIdAndStatus(
            @Param("productId") Long productId,
            @Param("status") OfferStatus status);

    // Kiểm tra buyer đã offer product này chưa (pending)
    Boolean existsByBuyerIdAndProductIdAndStatus(Long buyerId, Long productId, OfferStatus status);

    // Scheduler: offer hết hạn
    @Query("SELECT po FROM PriceOffer po WHERE po.status = 'PENDING' AND po.expiresAt <= :now")
    List<PriceOffer> findExpiredOffers(@Param("now") LocalDateTime now);

    Long countByBuyerId(Long buyerId);
    Long countBySellerId(Long sellerId);
}