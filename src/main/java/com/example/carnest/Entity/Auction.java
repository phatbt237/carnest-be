package com.example.carnest.Entity;

import com.example.carnest.Enum.AuctionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "auction", indexes = {
    @Index(name = "idx_auction_product", columnList = "product_id"),
    @Index(name = "idx_auction_seller", columnList = "seller_id"),
    @Index(name = "idx_auction_status", columnList = "status"),
    @Index(name = "idx_auction_end_time", columnList = "end_time")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Auction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    // ----- Cấu hình đấu giá -----
    @Column(name = "starting_price", nullable = false, precision = 12, scale = 0)
    private BigDecimal startingPrice;

    @Column(name = "reserve_price", precision = 12, scale = 0)
    private BigDecimal reservePrice;

    @Column(name = "bid_increment", nullable = false, precision = 12, scale = 0)
    private BigDecimal bidIncrement;

    @Column(name = "buy_now_price", precision = 12, scale = 0)
    private BigDecimal buyNowPrice;

    // ----- Thời gian -----
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    // ----- Anti-snipe -----
    @Column(name = "auto_extend_minutes")
    @Builder.Default
    private Integer autoExtendMinutes = 3;

    @Column(name = "snipe_threshold_min")
    @Builder.Default
    private Integer snipeThresholdMin = 2;

    @Column(name = "extended_count")
    @Builder.Default
    private Integer extendedCount = 0;

    // ----- Kết quả -----
    @Column(name = "current_price", precision = 12, scale = 0)
    private BigDecimal currentPrice;

    @Column(name = "total_bids")
    @Builder.Default
    private Integer totalBids = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_id")
    private User winner;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winning_bid_id")
    private AuctionBid winningBid;

    // ----- Trạng thái -----
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private AuctionStatus status = AuctionStatus.UPCOMING;

    // ----- Livestream -----
    @Column(name = "is_livestream")
    @Builder.Default
    private Boolean isLivestream = false;

    @Column(name = "livestream_url", length = 500)
    private String livestreamUrl;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ===== RELATIONSHIPS =====

    @OneToMany(mappedBy = "auction", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AuctionBid> bids = new ArrayList<>();

    @OneToMany(mappedBy = "auction", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AuctionWatch> watches = new ArrayList<>();
}
