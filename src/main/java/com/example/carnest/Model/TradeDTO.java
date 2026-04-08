package com.example.carnest.Model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class TradeDTO {

    public static class CreateRequest {
        @NotNull(message = "ID sản phẩm muốn đổi không được để trống")
        private Long targetProductId;
        @NotEmpty(message = "Phải có ít nhất 1 sản phẩm đưa ra đổi")
        private List<Long> offerProductIds;
        private BigDecimal cashOffset;
        private String message;

        public CreateRequest() {}
        public Long getTargetProductId() { return targetProductId; }
        public void setTargetProductId(Long targetProductId) { this.targetProductId = targetProductId; }
        public List<Long> getOfferProductIds() { return offerProductIds; }
        public void setOfferProductIds(List<Long> offerProductIds) { this.offerProductIds = offerProductIds; }
        public BigDecimal getCashOffset() { return cashOffset; }
        public void setCashOffset(BigDecimal cashOffset) { this.cashOffset = cashOffset; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class TradeResponse {
        private Long id;
        private String status;
        private String offererUsername;
        private String receiverUsername;
        private Long targetProductId;
        private String targetProductName;
        private BigDecimal cashOffset;
        private String message;
        private List<ProductInfo> offerProducts;
        private LocalDateTime expiresAt;
        private LocalDateTime createdAt;

        public TradeResponse() {}
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getOffererUsername() { return offererUsername; }
        public void setOffererUsername(String offererUsername) { this.offererUsername = offererUsername; }
        public String getReceiverUsername() { return receiverUsername; }
        public void setReceiverUsername(String receiverUsername) { this.receiverUsername = receiverUsername; }
        public Long getTargetProductId() { return targetProductId; }
        public void setTargetProductId(Long targetProductId) { this.targetProductId = targetProductId; }
        public String getTargetProductName() { return targetProductName; }
        public void setTargetProductName(String targetProductName) { this.targetProductName = targetProductName; }
        public BigDecimal getCashOffset() { return cashOffset; }
        public void setCashOffset(BigDecimal cashOffset) { this.cashOffset = cashOffset; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public List<ProductInfo> getOfferProducts() { return offerProducts; }
        public void setOfferProducts(List<ProductInfo> offerProducts) { this.offerProducts = offerProducts; }
        public LocalDateTime getExpiresAt() { return expiresAt; }
        public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    public static class ProductInfo {
        private Long id;
        private String name;

        public ProductInfo() {}
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}