package com.example.carnest.Entity;

import com.example.carnest.Enum.SaleType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "price_history", indexes = {
    @Index(name = "idx_price_history_brand", columnList = "brand_id"),
    @Index(name = "idx_price_history_car", columnList = "car_brand, car_model"),
    @Index(name = "idx_price_history_scale", columnList = "scale"),
    @Index(name = "idx_price_history_sold", columnList = "sold_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @Column(name = "car_brand", length = 100)
    private String carBrand;

    @Column(name = "car_model", length = 100)
    private String carModel;

    @Column(length = 10)
    private String scale;

    @Column(name = "product_condition", length = 20)
    private String condition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id")
    private Auction auction;

    @Column(name = "sold_price", nullable = false, precision = 12, scale = 0)
    private BigDecimal soldPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "sale_type", nullable = false, length = 20)
    private SaleType saleType;

    @Column(name = "sold_at")
    @Builder.Default
    private LocalDateTime soldAt = LocalDateTime.now();
}
