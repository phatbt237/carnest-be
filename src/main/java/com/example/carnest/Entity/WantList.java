package com.example.carnest.Entity;

import com.example.carnest.Enum.WantListStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "want_list", indexes = {
    @Index(name = "idx_want_list_user", columnList = "user_id"),
    @Index(name = "idx_want_list_status", columnList = "status"),
    @Index(name = "idx_want_list_brand", columnList = "brand_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WantList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(length = 10)
    private String scale;

    @Column(name = "car_brand", length = 100)
    private String carBrand;

    @Column(name = "car_model", length = 100)
    private String carModel;

    @Column(name = "condition_min", length = 20)
    private String conditionMin;

    @Column(name = "max_price", precision = 12, scale = 0)
    private BigDecimal maxPrice;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private WantListStatus status = WantListStatus.ACTIVE;

    @Column(name = "is_public")
    @Builder.Default
    private Boolean isPublic = true;

    @Column(name = "auto_notify")
    @Builder.Default
    private Boolean autoNotify = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ===== RELATIONSHIPS =====

    @OneToMany(mappedBy = "wantList", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<WantListMatch> matches = new ArrayList<>();
}
