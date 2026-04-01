package com.example.carnest.Model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PriceOfferDTO {

    public static class CreateRequest {
        @NotNull(message = "Product ID không được để trống")
        private Long productId;
        @NotNull @DecimalMin("1")
        private BigDecimal offerPrice;
        private String message;

        public CreateRequest() {}
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public BigDecimal getOfferPrice() { return offerPrice; }
        public void setOfferPrice(BigDecimal offerPrice) { this.offerPrice = offerPrice; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class CounterRequest {
        @NotNull @DecimalMin("1")
        private BigDecimal counterPrice;

        public CounterRequest() {}
        public BigDecimal getCounterPrice() { return counterPrice; }
        public void setCounterPrice(BigDecimal counterPrice) { this.counterPrice = counterPrice; }
    }

    public static class OfferResponse {
        private Long id;
        private Long productId;
        private String productName;
        private String productImage;
        private BigDecimal productPrice; // giá gốc
        private BigDecimal offerPrice;
        private BigDecimal counterPrice;
        private String message;
        private String status;
        private String buyerUsername;
        private String sellerUsername;
        private LocalDateTime expiresAt;
        private LocalDateTime respondedAt;
        private LocalDateTime createdAt;

        public OfferResponse() {}
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public String getProductImage() { return productImage; }
        public void setProductImage(String productImage) { this.productImage = productImage; }
        public BigDecimal getProductPrice() { return productPrice; }
        public void setProductPrice(BigDecimal productPrice) { this.productPrice = productPrice; }
        public BigDecimal getOfferPrice() { return offerPrice; }
        public void setOfferPrice(BigDecimal offerPrice) { this.offerPrice = offerPrice; }
        public BigDecimal getCounterPrice() { return counterPrice; }
        public void setCounterPrice(BigDecimal counterPrice) { this.counterPrice = counterPrice; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getBuyerUsername() { return buyerUsername; }
        public void setBuyerUsername(String buyerUsername) { this.buyerUsername = buyerUsername; }
        public String getSellerUsername() { return sellerUsername; }
        public void setSellerUsername(String sellerUsername) { this.sellerUsername = sellerUsername; }
        public LocalDateTime getExpiresAt() { return expiresAt; }
        public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
        public LocalDateTime getRespondedAt() { return respondedAt; }
        public void setRespondedAt(LocalDateTime respondedAt) { this.respondedAt = respondedAt; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }
}
