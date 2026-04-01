package com.example.carnest.Model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ShopDTO {

    // ===== TẠO SHOP =====
    public static class CreateShopRequest {

        @NotBlank(message = "Tên shop không được để trống")
        @Size(max = 200, message = "Tên shop tối đa 200 ký tự")
        private String shopName;

        @Size(max = 5000, message = "Mô tả tối đa 5000 ký tự")
        private String description;

        @Size(max = 500)
        private String logoUrl;

        @Size(max = 500)
        private String bannerUrl;

        private String returnPolicy;
        private String shippingInfo;

        public CreateShopRequest() {}

        public String getShopName() { return shopName; }
        public void setShopName(String shopName) { this.shopName = shopName; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getLogoUrl() { return logoUrl; }
        public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
        public String getBannerUrl() { return bannerUrl; }
        public void setBannerUrl(String bannerUrl) { this.bannerUrl = bannerUrl; }
        public String getReturnPolicy() { return returnPolicy; }
        public void setReturnPolicy(String returnPolicy) { this.returnPolicy = returnPolicy; }
        public String getShippingInfo() { return shippingInfo; }
        public void setShippingInfo(String shippingInfo) { this.shippingInfo = shippingInfo; }
    }

    // ===== CẬP NHẬT SHOP =====
    public static class UpdateShopRequest {

        @Size(max = 200, message = "Tên shop tối đa 200 ký tự")
        private String shopName;

        @Size(max = 5000)
        private String description;

        @Size(max = 500)
        private String logoUrl;

        @Size(max = 500)
        private String bannerUrl;

        private String returnPolicy;
        private String shippingInfo;

        public UpdateShopRequest() {}

        public String getShopName() { return shopName; }
        public void setShopName(String shopName) { this.shopName = shopName; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getLogoUrl() { return logoUrl; }
        public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
        public String getBannerUrl() { return bannerUrl; }
        public void setBannerUrl(String bannerUrl) { this.bannerUrl = bannerUrl; }
        public String getReturnPolicy() { return returnPolicy; }
        public void setReturnPolicy(String returnPolicy) { this.returnPolicy = returnPolicy; }
        public String getShippingInfo() { return shippingInfo; }
        public void setShippingInfo(String shippingInfo) { this.shippingInfo = shippingInfo; }
    }

    // ===== CURSOR RESPONSE — dùng cho mọi danh sách =====
    public static class CursorPage<T> {
        private List<T> items;
        private String nextCursor;    // cursor cho trang tiếp theo (null = hết)
        private boolean hasMore;
        private int size;
        private Long totalElements;   // tổng số record (optional)

        public CursorPage() {}
        public CursorPage(List<T> items, String nextCursor, boolean hasMore, int size, Long totalElements) {
            this.items = items; this.nextCursor = nextCursor;
            this.hasMore = hasMore; this.size = size; this.totalElements = totalElements;
        }

        public List<T> getItems() { return items; }
        public void setItems(List<T> items) { this.items = items; }
        public String getNextCursor() { return nextCursor; }
        public void setNextCursor(String nextCursor) { this.nextCursor = nextCursor; }
        public boolean isHasMore() { return hasMore; }
        public void setHasMore(boolean hasMore) { this.hasMore = hasMore; }
        public int getSize() { return size; }
        public void setSize(int size) { this.size = size; }
        public Long getTotalElements() { return totalElements; }
        public void setTotalElements(Long totalElements) { this.totalElements = totalElements; }
    }

    // ===== SHOP RESPONSE (chi tiết) =====
    public static class ShopResponse {
        private Long id;
        private String shopName;
        private String slug;
        private String description;
        private String logoUrl;
        private String bannerUrl;
        private String returnPolicy;
        private String shippingInfo;
        private Integer totalProducts;
        private Integer totalSold;
        private Integer followerCount;
        private BigDecimal ratingAvg;
        private Boolean isVerified;
        private Boolean isFollowing;
        private OwnerInfo owner;
        private LocalDateTime createdAt;

        public ShopResponse() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getShopName() { return shopName; }
        public void setShopName(String shopName) { this.shopName = shopName; }
        public String getSlug() { return slug; }
        public void setSlug(String slug) { this.slug = slug; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getLogoUrl() { return logoUrl; }
        public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
        public String getBannerUrl() { return bannerUrl; }
        public void setBannerUrl(String bannerUrl) { this.bannerUrl = bannerUrl; }
        public String getReturnPolicy() { return returnPolicy; }
        public void setReturnPolicy(String returnPolicy) { this.returnPolicy = returnPolicy; }
        public String getShippingInfo() { return shippingInfo; }
        public void setShippingInfo(String shippingInfo) { this.shippingInfo = shippingInfo; }
        public Integer getTotalProducts() { return totalProducts; }
        public void setTotalProducts(Integer totalProducts) { this.totalProducts = totalProducts; }
        public Integer getTotalSold() { return totalSold; }
        public void setTotalSold(Integer totalSold) { this.totalSold = totalSold; }
        public Integer getFollowerCount() { return followerCount; }
        public void setFollowerCount(Integer followerCount) { this.followerCount = followerCount; }
        public BigDecimal getRatingAvg() { return ratingAvg; }
        public void setRatingAvg(BigDecimal ratingAvg) { this.ratingAvg = ratingAvg; }
        public Boolean getIsVerified() { return isVerified; }
        public void setIsVerified(Boolean isVerified) { this.isVerified = isVerified; }
        public Boolean getIsFollowing() { return isFollowing; }
        public void setIsFollowing(Boolean isFollowing) { this.isFollowing = isFollowing; }
        public OwnerInfo getOwner() { return owner; }
        public void setOwner(OwnerInfo owner) { this.owner = owner; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    // ===== THÔNG TIN CHỦ SHOP =====
    public static class OwnerInfo {
        private Long id;
        private String username;
        private String fullName;
        private String avatarUrl;
        private BigDecimal sellerRatingAvg;
        private Integer totalSold;

        public OwnerInfo() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getAvatarUrl() { return avatarUrl; }
        public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
        public BigDecimal getSellerRatingAvg() { return sellerRatingAvg; }
        public void setSellerRatingAvg(BigDecimal sellerRatingAvg) { this.sellerRatingAvg = sellerRatingAvg; }
        public Integer getTotalSold() { return totalSold; }
        public void setTotalSold(Integer totalSold) { this.totalSold = totalSold; }
    }

    // ===== SHOP NGẮN GỌN (cho danh sách) =====
    public static class ShopSummary {
        private Long id;
        private String shopName;
        private String slug;
        private String logoUrl;
        private Integer totalProducts;
        private Integer followerCount;
        private BigDecimal ratingAvg;
        private Boolean isVerified;
        private Boolean isFollowing;

        public ShopSummary() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getShopName() { return shopName; }
        public void setShopName(String shopName) { this.shopName = shopName; }
        public String getSlug() { return slug; }
        public void setSlug(String slug) { this.slug = slug; }
        public String getLogoUrl() { return logoUrl; }
        public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
        public Integer getTotalProducts() { return totalProducts; }
        public void setTotalProducts(Integer totalProducts) { this.totalProducts = totalProducts; }
        public Integer getFollowerCount() { return followerCount; }
        public void setFollowerCount(Integer followerCount) { this.followerCount = followerCount; }
        public BigDecimal getRatingAvg() { return ratingAvg; }
        public void setRatingAvg(BigDecimal ratingAvg) { this.ratingAvg = ratingAvg; }
        public Boolean getIsVerified() { return isVerified; }
        public void setIsVerified(Boolean isVerified) { this.isVerified = isVerified; }
        public Boolean getIsFollowing() { return isFollowing; }
        public void setIsFollowing(Boolean isFollowing) { this.isFollowing = isFollowing; }
    }
}
