package com.example.carnest.Model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public class CartDTO {

    public static class AddToCartRequest {
        @NotNull(message = "Product ID không được để trống")
        private Long productId;
        @Min(value = 1, message = "Số lượng tối thiểu là 1")
        private Integer quantity = 1;

        public AddToCartRequest() {}
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }

    public static class CartResponse {
        private List<CartItemInfo> items;
        private int totalItems;
        private BigDecimal totalPrice;
        private int shopCount; // số shop khác nhau

        public CartResponse() {}
        public List<CartItemInfo> getItems() { return items; }
        public void setItems(List<CartItemInfo> items) { this.items = items; }
        public int getTotalItems() { return totalItems; }
        public void setTotalItems(int totalItems) { this.totalItems = totalItems; }
        public BigDecimal getTotalPrice() { return totalPrice; }
        public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
        public int getShopCount() { return shopCount; }
        public void setShopCount(int shopCount) { this.shopCount = shopCount; }
    }

    public static class CartItemInfo {
        private Long id;
        private Long productId;
        private String productName;
        private String productImage;
        private BigDecimal price;
        private Integer quantity;
        private String scale;
        private String condition;
        private Long shopId;
        private String shopName;
        private String shopSlug;
        private String brandName;
        private Boolean isAvailable; // sản phẩm còn bán không

        public CartItemInfo() {}
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
        public String getScale() { return scale; }
        public void setScale(String scale) { this.scale = scale; }
        public String getCondition() { return condition; }
        public void setCondition(String condition) { this.condition = condition; }
        public Long getShopId() { return shopId; }
        public void setShopId(Long shopId) { this.shopId = shopId; }
        public String getShopName() { return shopName; }
        public void setShopName(String shopName) { this.shopName = shopName; }
        public String getShopSlug() { return shopSlug; }
        public void setShopSlug(String shopSlug) { this.shopSlug = shopSlug; }
        public String getBrandName() { return brandName; }
        public void setBrandName(String brandName) { this.brandName = brandName; }
        public Boolean getIsAvailable() { return isAvailable; }
        public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }
    }
}
