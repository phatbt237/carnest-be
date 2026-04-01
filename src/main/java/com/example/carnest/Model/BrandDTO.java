package com.example.carnest.Model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class BrandDTO {

    public static class CreateRequest {
        @NotBlank(message = "Tên hãng không được để trống")
        @Size(max = 100)
        private String name;
        @Size(max = 500)
        private String logoUrl;
        @Size(max = 50)
        private String country;
        private String description;

        public CreateRequest() {}
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getLogoUrl() { return logoUrl; }
        public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class UpdateRequest {
        @Size(max = 100)
        private String name;
        @Size(max = 500)
        private String logoUrl;
        @Size(max = 50)
        private String country;
        private String description;
        private Boolean isActive;

        public UpdateRequest() {}
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getLogoUrl() { return logoUrl; }
        public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Boolean getIsActive() { return isActive; }
        public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    }

    public static class BrandResponse {
        private Long id;
        private String name;
        private String slug;
        private String logoUrl;
        private String country;
        private String description;
        private Boolean isActive;

        public BrandResponse() {}
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getSlug() { return slug; }
        public void setSlug(String slug) { this.slug = slug; }
        public String getLogoUrl() { return logoUrl; }
        public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Boolean getIsActive() { return isActive; }
        public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    }
}
