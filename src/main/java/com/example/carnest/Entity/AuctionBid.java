package com.example.carnest.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "auction_bid", indexes = {
    @Index(name = "idx_bid_auction", columnList = "auction_id"),
    @Index(name = "idx_bid_bidder", columnList = "bidder_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuctionBid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id", nullable = false)
    private Auction auction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bidder_id", nullable = false)
    private User bidder;

    @Column(name = "bid_amount", nullable = false, precision = 12, scale = 0)
    private BigDecimal bidAmount;

    @Column(name = "is_auto_bid")
    @Builder.Default
    private Boolean isAutoBid = false;

    @Column(name = "max_auto_bid", precision = 12, scale = 0)
    private BigDecimal maxAutoBid;

    @Column(name = "is_winning")
    @Builder.Default
    private Boolean isWinning = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
