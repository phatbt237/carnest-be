package com.example.carnest.Entity;

import com.example.carnest.Enum.TradeStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trade_offer", indexes = {
    @Index(name = "idx_trade_offerer", columnList = "offerer_id"),
    @Index(name = "idx_trade_receiver", columnList = "receiver_id"),
    @Index(name = "idx_trade_target", columnList = "target_product_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offerer_id", nullable = false)
    private User offerer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_product_id", nullable = false)
    private Product targetProduct;

    @Column(name = "cash_offset", precision = 12, scale = 0)
    @Builder.Default
    private BigDecimal cashOffset = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private TradeStatus status = TradeStatus.PENDING;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // ===== RELATIONSHIPS =====

    @OneToMany(mappedBy = "tradeOffer", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TradeOfferItem> offerItems = new ArrayList<>();
}
