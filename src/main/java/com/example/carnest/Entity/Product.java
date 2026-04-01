package com.example.carnest.Entity;

import com.example.carnest.Enum.ProductCondition;
import com.example.carnest.Enum.ProductStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product", indexes = {
    @Index(name = "idx_product_shop", columnList = "shop_id"),
    @Index(name = "idx_product_category", columnList = "category_id"),
    @Index(name = "idx_product_brand", columnList = "brand_id"),
    @Index(name = "idx_product_status", columnList = "status"),
    @Index(name = "idx_product_price", columnList = "price"),
    @Index(name = "idx_product_scale", columnList = "scale"),
    @Index(name = "idx_product_condition", columnList = "product_condition"),
    @Index(name = "idx_product_slug", columnList = "slug"),
    @Index(name = "idx_product_created", columnList = "created_at"),
    @Index(name = "idx_product_bulk_group", columnList = "bulk_group_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    // ----- Thông tin cơ bản -----
    @Column(nullable = false, length = 300)
    private String name;

    @Column(nullable = false, length = 300)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    // ----- Thông tin xe mô hình -----
    @Column(length = 10)
    private String scale;

    @Column(name = "car_brand", length = 100)
    private String carBrand;

    @Column(name = "car_model", length = 100)
    private String carModel;

    @Column(name = "year_made")
    private Integer yearMade;

    @Column(length = 50)
    private String color;

    @Column(length = 50)
    private String material;

    // ----- Tình trạng -----
    @Enumerated(EnumType.STRING)
    @Column(name = "product_condition", nullable = false, length = 20)
    private ProductCondition condition;

    @Column(name = "condition_note", columnDefinition = "TEXT")
    private String conditionNote;

    // ----- Giá & số lượng -----
    @Column(nullable = false, precision = 12, scale = 0)
    private BigDecimal price;

    @Column(name = "original_price", precision = 12, scale = 0)
    private BigDecimal originalPrice;

    @Column
    @Builder.Default
    private Integer quantity = 1;

    // ----- Phí ship -----
    @Column(name = "weight_gram")
    private Integer weightGram;

    @Column(name = "free_shipping")
    @Builder.Default
    private Boolean freeShipping = false;

    // ----- Combo / Bundle -----
    @Column(name = "is_combo")
    @Builder.Default
    private Boolean isCombo = false;

    @Column(name = "combo_quantity")
    @Builder.Default
    private Integer comboQuantity = 1;

    @Column(name = "bulk_group_id", length = 50)
    private String bulkGroupId;

    // ----- Xác thực -----
    @Column(name = "is_verified")
    @Builder.Default
    private Boolean isVerified = false;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by")
    private User verifiedBy;

    // ----- Giảm giá khi mua nhiều -----
    @Column(name = "bulk_discount_min")
    private Integer bulkDiscountMin;

    @Column(name = "bulk_discount_pct", precision = 5, scale = 2)
    private BigDecimal bulkDiscountPct;

    // ----- Đề xuất giá -----
    @Column(name = "allow_offer")
    @Builder.Default
    private Boolean allowOffer = true;

    @Column(name = "min_offer_price", precision = 12, scale = 0)
    private BigDecimal minOfferPrice;

    // ----- Thống kê -----
    @Column(name = "view_count")
    @Builder.Default
    private Integer viewCount = 0;

    @Column(name = "favorite_count")
    @Builder.Default
    private Integer favoriteCount = 0;

    @Column(name = "sold_count")
    @Builder.Default
    private Integer soldCount = 0;

    // ----- Trạng thái -----
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private ProductStatus status = ProductStatus.ACTIVE;

    // ----- SEO -----
    @Column(name = "meta_title", length = 200)
    private String metaTitle;

    @Column(name = "meta_description", length = 500)
    private String metaDescription;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ===== RELATIONSHIPS =====

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductVerification> verifications = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductFavorite> favorites = new ArrayList<>();
}
