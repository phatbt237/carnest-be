package com.example.carnest.Repository;

import com.example.carnest.Entity.AuctionBid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuctionBidRepository extends JpaRepository<AuctionBid, Long> {

    // Bid cao nhất của auction
    @Query("SELECT ab FROM AuctionBid ab WHERE ab.auction.id = :auctionId ORDER BY ab.bidAmount DESC")
    List<AuctionBid> findTopBids(@Param("auctionId") Long auctionId, @Param("limit") int limit);

    // Bid cao nhất
    @Query("SELECT ab FROM AuctionBid ab WHERE ab.auction.id = :auctionId AND ab.isWinning = true")
    Optional<AuctionBid> findWinningBid(@Param("auctionId") Long auctionId);

    // Lịch sử bid của auction
    @Query("SELECT ab FROM AuctionBid ab " +
            "JOIN FETCH ab.bidder " +
            "WHERE ab.auction.id = :auctionId " +
            "ORDER BY ab.bidAmount DESC")
    List<AuctionBid> findByAuctionIdWithBidder(@Param("auctionId") Long auctionId);

    // Auction mà user đã bid
    @Query("SELECT DISTINCT ab.auction.id FROM AuctionBid ab WHERE ab.bidder.id = :userId")
    List<Long> findAuctionIdsByBidderId(@Param("userId") Long userId);

    Long countByAuctionId(Long auctionId);
}