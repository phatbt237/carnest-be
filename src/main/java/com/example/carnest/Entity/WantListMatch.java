package com.example.carnest.Entity;

import com.example.carnest.Enum.MatchResponse;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "want_list_match",
       uniqueConstraints = @UniqueConstraint(columnNames = {"want_list_id", "product_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WantListMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "want_list_id", nullable = false)
    private WantList wantList;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "match_score", precision = 5, scale = 2)
    private BigDecimal matchScore;

    @Column(name = "is_notified")
    @Builder.Default
    private Boolean isNotified = false;

    @Column(name = "notified_at")
    private LocalDateTime notifiedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_response", length = 20)
    private MatchResponse userResponse;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
