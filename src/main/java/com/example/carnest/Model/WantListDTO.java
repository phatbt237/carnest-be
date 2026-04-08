package com.example.carnest.Model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class WantListDTO {

    public static class CreateRequest {
        @NotBlank(message = "Tiêu đề không được để trống")
        @Size(max = 300)
        private String title;
        private String description;
        @Size(max = 10)
        private String scale;
        @Size(max = 100)
        private String carBrand;
        @Size(max = 100)
        private String carModel;
        private BigDecimal maxPrice;
        private Boolean isPublic;

        public CreateRequest() {}
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getScale() { return scale; }
        public void setScale(String scale) { this.scale = scale; }
        public String getCarBrand() { return carBrand; }
        public void setCarBrand(String carBrand) { this.carBrand = carBrand; }
        public String getCarModel() { return carModel; }
        public void setCarModel(String carModel) { this.carModel = carModel; }
        public BigDecimal getMaxPrice() { return maxPrice; }
        public void setMaxPrice(BigDecimal maxPrice) { this.maxPrice = maxPrice; }
        public Boolean getIsPublic() { return isPublic; }
        public void setIsPublic(Boolean isPublic) { this.isPublic = isPublic; }
    }

    public static class WantListResponse {
        private Long id;
        private String title;
        private String description;
        private String scale;
        private String carBrand;
        private String carModel;
        private BigDecimal maxPrice;
        private String status;
        private Boolean isPublic;
        private String username;
        private LocalDateTime createdAt;

        public WantListResponse() {}
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getScale() { return scale; }
        public void setScale(String scale) { this.scale = scale; }
        public String getCarBrand() { return carBrand; }
        public void setCarBrand(String carBrand) { this.carBrand = carBrand; }
        public String getCarModel() { return carModel; }
        public void setCarModel(String carModel) { this.carModel = carModel; }
        public BigDecimal getMaxPrice() { return maxPrice; }
        public void setMaxPrice(BigDecimal maxPrice) { this.maxPrice = maxPrice; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Boolean getIsPublic() { return isPublic; }
        public void setIsPublic(Boolean isPublic) { this.isPublic = isPublic; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }
}