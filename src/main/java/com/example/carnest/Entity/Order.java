package com.example.carnest.Entity;

import com.example.carnest.Enum.EscrowStatus;
import com.example.carnest.Enum.OrderStatus;
import com.example.carnest.Enum.PaymentMethod;
import com.example.carnest.Enum.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_order_buyer", columnList = "buyer_id"),
    @Index(name = "idx_order_shop", columnList = "shop_id"),
    @Index(name = "idx_order_status", columnList = "status"),
    @Index(name = "idx_order_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_code", nullable = false, unique = true, length = 30)
    private String orderCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    // ----- Địa chỉ giao hàng (snapshot) -----
    @Column(name = "shipping_name", nullable = false, length = 150)
    private String shippingName;

    @Column(name = "shipping_phone", nullable = false, length = 20)
    private String shippingPhone;

    @Column(name = "shipping_address", nullable = false, columnDefinition = "TEXT")
    private String shippingAddress;

    // ----- Giá -----
    @Column(nullable = false, precision = 12, scale = 0)
    private BigDecimal subtotal;

    @Column(name = "shipping_fee", precision = 12, scale = 0)
    @Builder.Default
    private BigDecimal shippingFee = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 12, scale = 0)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "total_quantity")
    private Integer totalQuantity;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 0)
    private BigDecimal totalAmount;

    // ----- Thanh toán -----
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 30)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", length = 20)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    // ----- Escrow -----
    @Enumerated(EnumType.STRING)
    @Column(name = "escrow_status", length = 20)
    @Builder.Default
    private EscrowStatus escrowStatus = EscrowStatus.NONE;

    @Column(name = "escrow_released_at")
    private LocalDateTime escrowReleasedAt;

    // ----- Vận chuyển -----
    @Column(name = "shipping_method", length = 50)
    private String shippingMethod;

    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;

    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    // ----- Trạng thái -----
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    // ----- Ghi chú -----
    @Column(name = "buyer_note", columnDefinition = "TEXT")
    private String buyerNote;

    @Column(name = "seller_note", columnDefinition = "TEXT")
    private String sellerNote;

    @Column(name = "cancel_reason", columnDefinition = "TEXT")
    private String cancelReason;

    @Column(name = "auto_complete_at")
    private LocalDateTime autoCompleteAt;

    @Column(name = "payment_deadline")
    private LocalDateTime paymentDeadline;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ===== RELATIONSHIPS =====

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderStatusHistory> statusHistory = new ArrayList<>();
}
