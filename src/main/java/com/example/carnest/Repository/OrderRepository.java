package com.example.carnest.Repository;

import com.example.carnest.Entity.Order;
import com.example.carnest.Enum.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT o FROM Order o " +
           "JOIN FETCH o.shop s JOIN FETCH s.user " +
           "JOIN FETCH o.buyer " +
           "WHERE o.id = :id")
    Optional<Order> findByIdFull(@Param("id") Long id);

    @Query("SELECT o FROM Order o " +
           "JOIN FETCH o.shop s " +
           "WHERE o.orderCode = :code")
    Optional<Order> findByOrderCode(@Param("code") String code);

    // Đơn hàng của buyer — cursor-based
    @Query("SELECT o FROM Order o " +
           "JOIN FETCH o.shop s " +
           "WHERE o.buyer.id = :buyerId " +
           "AND (:status IS NULL OR o.status = :status) " +
           "AND (:cursorId IS NULL OR o.id < :cursorId) " +
           "ORDER BY o.id DESC")
    List<Order> findByBuyerId(
            @Param("buyerId") Long buyerId,
            @Param("status") OrderStatus status,
            @Param("cursorId") Long cursorId,
            @Param("limit") int limit);

    // Đơn hàng của seller (shop) — cursor-based
    @Query("SELECT o FROM Order o " +
           "JOIN FETCH o.buyer " +
           "WHERE o.shop.id = :shopId " +
           "AND (:status IS NULL OR o.status = :status) " +
           "AND (:cursorId IS NULL OR o.id < :cursorId) " +
           "ORDER BY o.id DESC")
    List<Order> findByShopId(
            @Param("shopId") Long shopId,
            @Param("status") OrderStatus status,
            @Param("cursorId") Long cursorId,
            @Param("limit") int limit);

    // Đếm
    @Query("SELECT COUNT(o) FROM Order o WHERE o.buyer.id = :buyerId AND (:status IS NULL OR o.status = :status)")
    Long countByBuyerId(@Param("buyerId") Long buyerId, @Param("status") OrderStatus status);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.shop.id = :shopId AND (:status IS NULL OR o.status = :status)")
    Long countByShopId(@Param("shopId") Long shopId, @Param("status") OrderStatus status);

    // Auto-complete: đơn đã delivered quá hạn
    @Query("SELECT o FROM Order o WHERE o.status = 'DELIVERED' AND o.autoCompleteAt <= :now")
    List<Order> findOrdersToAutoComplete(@Param("now") LocalDateTime now);

    @Query("SELECT o FROM Order o WHERE o.status = 'PENDING_PAYMENT' AND o.paymentDeadline <= :now")
    List<Order> findExpiredPendingPayment(@Param("now") LocalDateTime now);
}
