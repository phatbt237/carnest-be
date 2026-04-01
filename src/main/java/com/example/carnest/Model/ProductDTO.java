package com.example.carnest.Model;

import com.example.carnest.Enum.ProductCondition;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ProductDTO {

    // ===== TẠO SẢN PHẨM =====
    public static class CreateRequest {
        @NotBlank(message = "Tên sản phẩm không được để trống")
        @Size(max = 300)
        private String name;
        private String description;
        private Long categoryId;
        private Long brandId;

        // Thông tin xe
        @Size(max = 10)
        private String scale;
        @Size(max = 100)
        private String carBrand;
        @Size(max = 100)
        private String carModel;
        private Integer yearMade;
        @Size(max = 50)
        private String color;
        @Size(max = 50)
        private String material;

        // Tình trạng
        @NotNull(message = "Tình trạng không được để trống")
        private ProductCondition condition;
        private String conditionNote;

        // Giá
        @NotNull(message = "Giá không được để trống")
        @DecimalMin(value = "0", message = "Giá phải >= 0")
        private BigDecimal price;
        private BigDecimal originalPrice;
        private Integer quantity;
        private Integer weightGram;
        private Boolean freeShipping;

        // Combo
        private Boolean isCombo;
        private Integer comboQuantity;

        // Offer
        private Boolean allowOffer;
        private BigDecimal minOfferPrice;

        // Giảm giá khi mua nhiều
        private Integer bulkDiscountMin;
        private BigDecimal bulkDiscountPct;

        // Ảnh
        private List<String> imageUrls;

        // SEO
        private String metaTitle;
        private String metaDescription;

        public CreateRequest() {}

        // Getters & Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Long getCategoryId() { return categoryId; }
        public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
        public Long getBrandId() { return brandId; }
        public void setBrandId(Long brandId) { this.brandId = brandId; }
        public String getScale() { return scale; }
        public void setScale(String scale) { this.scale = scale; }
        public String getCarBrand() { return carBrand; }
        public void setCarBrand(String carBrand) { this.carBrand = carBrand; }
        public String getCarModel() { return carModel; }
        public void setCarModel(String carModel) { this.carModel = carModel; }
        public Integer getYearMade() { return yearMade; }
        public void setYearMade(Integer yearMade) { this.yearMade = yearMade; }
        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
        public String getMaterial() { return material; }
        public void setMaterial(String material) { this.material = material; }
        public ProductCondition getCondition() { return condition; }
        public void setCondition(ProductCondition condition) { this.condition = condition; }
        public String getConditionNote() { return conditionNote; }
        public void setConditionNote(String conditionNote) { this.conditionNote = conditionNote; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public BigDecimal getOriginalPrice() { return originalPrice; }
        public void setOriginalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public Integer getWeightGram() { return weightGram; }
        public void setWeightGram(Integer weightGram) { this.weightGram = weightGram; }
        public Boolean getFreeShipping() { return freeShipping; }
        public void setFreeShipping(Boolean freeShipping) { this.freeShipping = freeShipping; }
        public Boolean getIsCombo() { return isCombo; }
        public void setIsCombo(Boolean isCombo) { this.isCombo = isCombo; }
        public Integer getComboQuantity() { return comboQuantity; }
        public void setComboQuantity(Integer comboQuantity) { this.comboQuantity = comboQuantity; }
        public Boolean getAllowOffer() { return allowOffer; }
        public void setAllowOffer(Boolean allowOffer) { this.allowOffer = allowOffer; }
        public BigDecimal getMinOfferPrice() { return minOfferPrice; }
        public void setMinOfferPrice(BigDecimal minOfferPrice) { this.minOfferPrice = minOfferPrice; }
        public Integer getBulkDiscountMin() { return bulkDiscountMin; }
        public void setBulkDiscountMin(Integer bulkDiscountMin) { this.bulkDiscountMin = bulkDiscountMin; }
        public BigDecimal getBulkDiscountPct() { return bulkDiscountPct; }
        public void setBulkDiscountPct(BigDecimal bulkDiscountPct) { this.bulkDiscountPct = bulkDiscountPct; }
        public List<String> getImageUrls() { return imageUrls; }
        public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
        public String getMetaTitle() { return metaTitle; }
        public void setMetaTitle(String metaTitle) { this.metaTitle = metaTitle; }
        public String getMetaDescription() { return metaDescription; }
        public void setMetaDescription(String metaDescription) { this.metaDescription = metaDescription; }
    }

    // ===== PRODUCT RESPONSE (chi tiết) =====
    public static class ProductResponse {
        private Long id;
        private String name;
        private String slug;
        private String description;
        private String scale;
        private String carBrand;
        private String carModel;
        private Integer yearMade;
        private String color;
        private String material;
        private String condition;
        private String conditionNote;
        private BigDecimal price;
        private BigDecimal originalPrice;
        private Integer quantity;
        private Integer weightGram;
        private Boolean freeShipping;
        private Boolean isCombo;
        private Integer comboQuantity;
        private Boolean isVerified;
        private Boolean allowOffer;
        private BigDecimal minOfferPrice;
        private Integer bulkDiscountMin;
        private BigDecimal bulkDiscountPct;
        private Integer viewCount;
        private Integer favoriteCount;
        private Integer soldCount;
        private String status;
        private List<ImageInfo> images;
        private ShopInfo shop;
        private CategoryInfo category;
        private BrandInfo brand;
        private LocalDateTime createdAt;

        public ProductResponse() {}

        // Getters & Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getSlug() { return slug; }
        public void setSlug(String slug) { this.slug = slug; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getScale() { return scale; }
        public void setScale(String scale) { this.scale = scale; }
        public String getCarBrand() { return carBrand; }
        public void setCarBrand(String carBrand) { this.carBrand = carBrand; }
        public String getCarModel() { return carModel; }
        public void setCarModel(String carModel) { this.carModel = carModel; }
        public Integer getYearMade() { return yearMade; }
        public void setYearMade(Integer yearMade) { this.yearMade = yearMade; }
        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
        public String getMaterial() { return material; }
        public void setMaterial(String material) { this.material = material; }
        public String getCondition() { return condition; }
        public void setCondition(String condition) { this.condition = condition; }
        public String getConditionNote() { return conditionNote; }
        public void setConditionNote(String conditionNote) { this.conditionNote = conditionNote; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public BigDecimal getOriginalPrice() { return originalPrice; }
        public void setOriginalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public Integer getWeightGram() { return weightGram; }
        public void setWeightGram(Integer weightGram) { this.weightGram = weightGram; }
        public Boolean getFreeShipping() { return freeShipping; }
        public void setFreeShipping(Boolean freeShipping) { this.freeShipping = freeShipping; }
        public Boolean getIsCombo() { return isCombo; }
        public void setIsCombo(Boolean isCombo) { this.isCombo = isCombo; }
        public Integer getComboQuantity() { return comboQuantity; }
        public void setComboQuantity(Integer comboQuantity) { this.comboQuantity = comboQuantity; }
        public Boolean getIsVerified() { return isVerified; }
        public void setIsVerified(Boolean isVerified) { this.isVerified = isVerified; }
        public Boolean getAllowOffer() { return allowOffer; }
        public void setAllowOffer(Boolean allowOffer) { this.allowOffer = allowOffer; }
        public BigDecimal getMinOfferPrice() { return minOfferPrice; }
        public void setMinOfferPrice(BigDecimal minOfferPrice) { this.minOfferPrice = minOfferPrice; }
        public Integer getBulkDiscountMin() { return bulkDiscountMin; }
        public void setBulkDiscountMin(Integer bulkDiscountMin) { this.bulkDiscountMin = bulkDiscountMin; }
        public BigDecimal getBulkDiscountPct() { return bulkDiscountPct; }
        public void setBulkDiscountPct(BigDecimal bulkDiscountPct) { this.bulkDiscountPct = bulkDiscountPct; }
        public Integer getViewCount() { return viewCount; }
        public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }
        public Integer getFavoriteCount() { return favoriteCount; }
        public void setFavoriteCount(Integer favoriteCount) { this.favoriteCount = favoriteCount; }
        public Integer getSoldCount() { return soldCount; }
        public void setSoldCount(Integer soldCount) { this.soldCount = soldCount; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public List<ImageInfo> getImages() { return images; }
        public void setImages(List<ImageInfo> images) { this.images = images; }
        public ShopInfo getShop() { return shop; }
        public void setShop(ShopInfo shop) { this.shop = shop; }
        public CategoryInfo getCategory() { return category; }
        public void setCategory(CategoryInfo category) { this.category = category; }
        public BrandInfo getBrand() { return brand; }
        public void setBrand(BrandInfo brand) { this.brand = brand; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    // ===== PRODUCT SUMMARY (cho danh sách) =====
    public static class ProductSummary {
        private Long id;
        private String name;
        private String slug;
        private String primaryImage;
        private BigDecimal price;
        private BigDecimal originalPrice;
        private String scale;
        private String condition;
        private Boolean isVerified;
        private Boolean freeShipping;
        private String shopName;
        private String shopSlug;
        private String brandName;
        private LocalDateTime createdAt;

        public ProductSummary() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getSlug() { return slug; }
        public void setSlug(String slug) { this.slug = slug; }
        public String getPrimaryImage() { return primaryImage; }
        public void setPrimaryImage(String primaryImage) { this.primaryImage = primaryImage; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public BigDecimal getOriginalPrice() { return originalPrice; }
        public void setOriginalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; }
        public String getScale() { return scale; }
        public void setScale(String scale) { this.scale = scale; }
        public String getCondition() { return condition; }
        public void setCondition(String condition) { this.condition = condition; }
        public Boolean getIsVerified() { return isVerified; }
        public void setIsVerified(Boolean isVerified) { this.isVerified = isVerified; }
        public Boolean getFreeShipping() { return freeShipping; }
        public void setFreeShipping(Boolean freeShipping) { this.freeShipping = freeShipping; }
        public String getShopName() { return shopName; }
        public void setShopName(String shopName) { this.shopName = shopName; }
        public String getShopSlug() { return shopSlug; }
        public void setShopSlug(String shopSlug) { this.shopSlug = shopSlug; }
        public String getBrandName() { return brandName; }
        public void setBrandName(String brandName) { this.brandName = brandName; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    // ===== SUB DTOs =====
    public static class ImageInfo {
        private Long id;
        private String imageUrl;
        private String thumbnailUrl;
        private Boolean isPrimary;

        public ImageInfo() {}
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        public String getThumbnailUrl() { return thumbnailUrl; }
        public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
        public Boolean getIsPrimary() { return isPrimary; }
        public void setIsPrimary(Boolean isPrimary) { this.isPrimary = isPrimary; }
    }

    public static class ShopInfo {
        private Long id;
        private String shopName;
        private String slug;
        private String logoUrl;

        public ShopInfo() {}
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getShopName() { return shopName; }
        public void setShopName(String shopName) { this.shopName = shopName; }
        public String getSlug() { return slug; }
        public void setSlug(String slug) { this.slug = slug; }
        public String getLogoUrl() { return logoUrl; }
        public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    }

    public static class CategoryInfo {
        private Long id;
        private String name;
        private String slug;

        public CategoryInfo() {}
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getSlug() { return slug; }
        public void setSlug(String slug) { this.slug = slug; }
    }

    public static class BrandInfo {
        private Long id;
        private String name;
        private String slug;

        public BrandInfo() {}
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getSlug() { return slug; }
        public void setSlug(String slug) { this.slug = slug; }
    }
}
