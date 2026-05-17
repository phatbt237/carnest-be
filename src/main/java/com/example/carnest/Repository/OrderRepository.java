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

    // ===== ADMIN REPORT =====
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    Long countByStatus(@Param("status") OrderStatus status);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt BETWEEN :from AND :to")
    Long countByPeriod(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = 'COMPLETED' AND o.createdAt BETWEEN :from AND :to")
    java.math.BigDecimal sumRevenueByPeriod(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = 'COMPLETED'")
    java.math.BigDecimal sumTotalRevenue();

    // Doanh thu theo ngày: trả về [date_str, sum]
    @Query("SELECT FUNCTION('DATE_FORMAT', o.createdAt, '%Y-%m-%d'), COALESCE(SUM(o.totalAmount), 0), COUNT(o) " +
           "FROM Order o WHERE o.status = 'COMPLETED' AND o.createdAt BETWEEN :from AND :to " +
           "GROUP BY FUNCTION('DATE_FORMAT', o.createdAt, '%Y-%m-%d') ORDER BY 1 ASC")
    List<Object[]> revenueGroupByDay(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // Doanh thu theo tháng
    @Query("SELECT FUNCTION('DATE_FORMAT', o.createdAt, '%Y-%m'), COALESCE(SUM(o.totalAmount), 0), COUNT(o) " +
           "FROM Order o WHERE o.status = 'COMPLETED' AND o.createdAt BETWEEN :from AND :to " +
           "GROUP BY FUNCTION('DATE_FORMAT', o.createdAt, '%Y-%m') ORDER BY 1 ASC")
    List<Object[]> revenueGroupByMonth(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // Top shop theo doanh thu
    @Query("SELECT o.shop.id, o.shop.shopName, COALESCE(SUM(o.totalAmount), 0), COUNT(o) " +
           "FROM Order o WHERE o.status = 'COMPLETED' " +
           "GROUP BY o.shop.id, o.shop.shopName ORDER BY 3 DESC")
    List<Object[]> topShopsByRevenue(@Param("limit") int limit);

    // Top buyer theo tổng chi
    @Query("SELECT o.buyer.id, o.buyer.username, o.buyer.fullName, COALESCE(SUM(o.totalAmount), 0), COUNT(o) " +
           "FROM Order o WHERE o.status = 'COMPLETED' " +
           "GROUP BY o.buyer.id, o.buyer.username, o.buyer.fullName ORDER BY 4 DESC")
    List<Object[]> topBuyersBySpend(@Param("limit") int limit);

    // Phân bổ trạng thái đơn
    @Query("SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status")
    List<Object[]> countGroupByStatus();
}
