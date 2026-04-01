package com.example.carnest.Model;

import com.example.carnest.Enum.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderDTO {

    // ===== CHECKOUT REQUEST =====
    public static class CheckoutRequest {
        @NotBlank(message = "Tên người nhận không được để trống")
        private String shippingName;
        @NotBlank(message = "Số điện thoại không được để trống")
        private String shippingPhone;
        @NotBlank(message = "Địa chỉ không được để trống")
        private String shippingAddress;
        @NotNull(message = "Phương thức thanh toán không được để trống")
        private PaymentMethod paymentMethod;
        private String buyerNote;
        // Nếu null → checkout toàn bộ giỏ hàng
        // Nếu có → checkout các product ID chỉ định
        private List<Long> productIds;

        public CheckoutRequest() {}
        public String getShippingName() { return shippingName; }
        public void setShippingName(String shippingName) { this.shippingName = shippingName; }
        public String getShippingPhone() { return shippingPhone; }
        public void setShippingPhone(String shippingPhone) { this.shippingPhone = shippingPhone; }
        public String getShippingAddress() { return shippingAddress; }
        public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
        public PaymentMethod getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
        public String getBuyerNote() { return buyerNote; }
        public void setBuyerNote(String buyerNote) { this.buyerNote = buyerNote; }
        public List<Long> getProductIds() { return productIds; }
        public void setProductIds(List<Long> productIds) { this.productIds = productIds; }
    }

    // ===== ORDER RESPONSE =====
    public static class OrderResponse {
        private Long id;
        private String orderCode;
        private String status;
        private String paymentMethod;
        private String paymentStatus;
        private String escrowStatus;
        private BigDecimal subtotal;
        private BigDecimal shippingFee;
        private BigDecimal discountAmount;
        private Integer totalQuantity;
        private BigDecimal totalAmount;
        private String shippingName;
        private String shippingPhone;
        private String shippingAddress;
        private String shippingMethod;
        private String trackingNumber;
        private String buyerNote;
        private String sellerNote;
        private String cancelReason;
        private List<OrderItemInfo> items;
        private ShopSummary shop;
        private BuyerSummary buyer;
        private List<StatusHistoryInfo> statusHistory;
        private LocalDateTime createdAt;
        private LocalDateTime paidAt;
        private LocalDateTime shippedAt;
        private LocalDateTime deliveredAt;
        private LocalDateTime autoCompleteAt;

        public OrderResponse() {}
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getOrderCode() { return orderCode; }
        public void setOrderCode(String orderCode) { this.orderCode = orderCode; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
        public String getPaymentStatus() { return paymentStatus; }
        public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
        public String getEscrowStatus() { return escrowStatus; }
        public void setEscrowStatus(String escrowStatus) { this.escrowStatus = escrowStatus; }
        public BigDecimal getSubtotal() { return subtotal; }
        public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
        public BigDecimal getShippingFee() { return shippingFee; }
        public void setShippingFee(BigDecimal shippingFee) { this.shippingFee = shippingFee; }
        public BigDecimal getDiscountAmount() { return discountAmount; }
        public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
        public Integer getTotalQuantity() { return totalQuantity; }
        public void setTotalQuantity(Integer totalQuantity) { this.totalQuantity = totalQuantity; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
        public String getShippingName() { return shippingName; }
        public void setShippingName(String shippingName) { this.shippingName = shippingName; }
        public String getShippingPhone() { return shippingPhone; }
        public void setShippingPhone(String shippingPhone) { this.shippingPhone = shippingPhone; }
        public String getShippingAddress() { return shippingAddress; }
        public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
        public String getShippingMethod() { return shippingMethod; }
        public void setShippingMethod(String shippingMethod) { this.shippingMethod = shippingMethod; }
        public String getTrackingNumber() { return trackingNumber; }
        public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }
        public String getBuyerNote() { return buyerNote; }
        public void setBuyerNote(String buyerNote) { this.buyerNote = buyerNote; }
        public String getSellerNote() { return sellerNote; }
        public void setSellerNote(String sellerNote) { this.sellerNote = sellerNote; }
        public String getCancelReason() { return cancelReason; }
        public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }
        public List<OrderItemInfo> getItems() { return items; }
        public void setItems(List<OrderItemInfo> items) { this.items = items; }
        public ShopSummary getShop() { return shop; }
        public void setShop(ShopSummary shop) { this.shop = shop; }
        public BuyerSummary getBuyer() { return buyer; }
        public void setBuyer(BuyerSummary buyer) { this.buyer = buyer; }
        public List<StatusHistoryInfo> getStatusHistory() { return statusHistory; }
        public void setStatusHistory(List<StatusHistoryInfo> statusHistory) { this.statusHistory = statusHistory; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        public LocalDateTime getPaidAt() { return paidAt; }
        public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
        public LocalDateTime getShippedAt() { return shippedAt; }
        public void setShippedAt(LocalDateTime shippedAt) { this.shippedAt = shippedAt; }
        public LocalDateTime getDeliveredAt() { return deliveredAt; }
        public void setDeliveredAt(LocalDateTime deliveredAt) { this.deliveredAt = deliveredAt; }
        public LocalDateTime getAutoCompleteAt() { return autoCompleteAt; }
        public void setAutoCompleteAt(LocalDateTime autoCompleteAt) { this.autoCompleteAt = autoCompleteAt; }
    }

    // ===== SUB DTOs =====
    public static class OrderItemInfo {
        private Long id;
        private Long productId;
        private String productName;
        private String productImage;
        private BigDecimal price;
        private Integer quantity;

        public OrderItemInfo() {}
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public String getProductImage() { return productImage; }
        public void setProductImage(String productImage) { this.productImage = productImage; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }

    public static class ShopSummary {
        private Long id;
        private String shopName;
        private String slug;

        public ShopSummary() {}
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getShopName() { return shopName; }
        public void setShopName(String shopName) { this.shopName = shopName; }
        public String getSlug() { return slug; }
        public void setSlug(String slug) { this.slug = slug; }
    }

    public static class BuyerSummary {
        private Long id;
        private String username;
        private String fullName;

        public BuyerSummary() {}
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
    }

    public static class StatusHistoryInfo {
        private String fromStatus;
        private String toStatus;
        private String note;
        private LocalDateTime createdAt;

        public StatusHistoryInfo() {}
        public String getFromStatus() { return fromStatus; }
        public void setFromStatus(String fromStatus) { this.fromStatus = fromStatus; }
        public String getToStatus() { return toStatus; }
        public void setToStatus(String toStatus) { this.toStatus = toStatus; }
        public String getNote() { return note; }
        public void setNote(String note) { this.note = note; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    // ===== CẬP NHẬT TRẠNG THÁI =====
    public static class UpdateStatusRequest {
        private String note;
        private String trackingNumber;
        private String shippingMethod;
        private String cancelReason;

        public UpdateStatusRequest() {}
        public String getNote() { return note; }
        public void setNote(String note) { this.note = note; }
        public String getTrackingNumber() { return trackingNumber; }
        public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }
        public String getShippingMethod() { return shippingMethod; }
        public void setShippingMethod(String shippingMethod) { this.shippingMethod = shippingMethod; }
        public String getCancelReason() { return cancelReason; }
        public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }
    }

    // ===== WALLET DTO =====
    public static class WalletResponse {
        private BigDecimal balance;
        private BigDecimal pendingBalance;

        public WalletResponse() {}
        public BigDecimal getBalance() { return balance; }
        public void setBalance(BigDecimal balance) { this.balance = balance; }
        public BigDecimal getPendingBalance() { return pendingBalance; }
        public void setPendingBalance(BigDecimal pendingBalance) { this.pendingBalance = pendingBalance; }
    }

    public static class WalletTransactionInfo {
        private Long id;
        private String type;
        private BigDecimal amount;
        private BigDecimal balanceAfter;
        private String description;
        private LocalDateTime createdAt;

        public WalletTransactionInfo() {}
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public BigDecimal getBalanceAfter() { return balanceAfter; }
        public void setBalanceAfter(BigDecimal balanceAfter) { this.balanceAfter = balanceAfter; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }
}
