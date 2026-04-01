package com.example.carnest.Repository;

import com.example.carnest.Entity.Auction;
import com.example.carnest.Enum.AuctionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Long> {

    @Query("SELECT a FROM Auction a " +
            "JOIN FETCH a.product p JOIN FETCH p.shop s JOIN FETCH s.user " +
            "LEFT JOIN FETCH p.brand " +
            "WHERE a.id = :id")
    Optional<Auction> findByIdFull(@Param("id") Long id);

    // Danh sách auction active — cursor-based
    @Query("SELECT a FROM Auction a " +
            "JOIN FETCH a.product p JOIN FETCH p.shop s JOIN FETCH s.user " +
            "WHERE a.status = :status " +
            "AND (:cursorId IS NULL OR a.id < :cursorId) " +
            "ORDER BY a.id DESC")
    List<Auction> findByStatus(
            @Param("status") AuctionStatus status,
            @Param("cursorId") Long cursorId,
            @Param("limit") int limit);

    // Auction sắp kết thúc (ending soon)
    @Query("SELECT a FROM Auction a " +
            "JOIN FETCH a.product p JOIN FETCH p.shop s JOIN FETCH s.user " +
            "WHERE a.status = 'ACTIVE' " +
            "AND a.endTime <= :deadline " +
            "AND (:cursorId IS NULL OR a.id < :cursorId) " +
            "ORDER BY a.endTime ASC")
    List<Auction> findEndingSoon(
            @Param("deadline") LocalDateTime deadline,
            @Param("cursorId") Long cursorId,
            @Param("limit") int limit);

    // Auction của seller
    @Query("SELECT a FROM Auction a " +
            "JOIN FETCH a.product p " +
            "WHERE a.seller.id = :sellerId " +
            "AND (:cursorId IS NULL OR a.id < :cursorId) " +
            "ORDER BY a.id DESC")
    List<Auction> findBySellerId(
            @Param("sellerId") Long sellerId,
            @Param("cursorId") Long cursorId,
            @Param("limit") int limit);

    // Scheduler: auction hết thời gian
    @Query("SELECT a FROM Auction a WHERE a.status = 'ACTIVE' AND a.endTime <= :now")
    List<Auction> findExpiredAuctions(@Param("now") LocalDateTime now);

    // Scheduler: auction sắp bắt đầu
    @Query("SELECT a FROM Auction a WHERE a.status = 'UPCOMING' AND a.startTime <= :now")
    List<Auction> findAuctionsToStart(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(a) FROM Auction a WHERE a.status = :status")
    Long countByStatus(@Param("status") AuctionStatus status);
}