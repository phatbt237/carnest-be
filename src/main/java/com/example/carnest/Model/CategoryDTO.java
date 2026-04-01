package com.example.carnest.Model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public class CategoryDTO {

    public static class CreateRequest {
        @NotBlank(message = "Tên danh mục không được để trống")
        @Size(max = 100)
        private String name;
        private Long parentId;
        @Size(max = 500)
        private String iconUrl;
        private Integer sortOrder;

        public CreateRequest() {}
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Long getParentId() { return parentId; }
        public void setParentId(Long parentId) { this.parentId = parentId; }
        public String getIconUrl() { return iconUrl; }
        public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }
        public Integer getSortOrder() { return sortOrder; }
        public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    }

    public static class UpdateRequest {
        @Size(max = 100)
        private String name;
        @Size(max = 500)
        private String iconUrl;
        private Integer sortOrder;
        private Boolean isActive;

        public UpdateRequest() {}
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getIconUrl() { return iconUrl; }
        public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }
        public Integer getSortOrder() { return sortOrder; }
        public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
        public Boolean getIsActive() { return isActive; }
        public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    }

    public static class CategoryResponse {
        private Long id;
        private String name;
        private String slug;
        private String iconUrl;
        private Integer sortOrder;
        private Boolean isActive;
        private Long parentId;
        private String parentName;
        private List<CategoryResponse> children;

        public CategoryResponse() {}
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getSlug() { return slug; }
        public void setSlug(String slug) { this.slug = slug; }
        public String getIconUrl() { return iconUrl; }
        public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }
        public Integer getSortOrder() { return sortOrder; }
        public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
        public Boolean getIsActive() { return isActive; }
        public void setIsActive(Boolean isActive) { this.isActive = isActive; }
        public Long getParentId() { return parentId; }
        public void setParentId(Long parentId) { this.parentId = parentId; }
        public String getParentName() { return parentName; }
        public void setParentName(String parentName) { this.parentName = parentName; }
        public List<CategoryResponse> getChildren() { return children; }
        public void setChildren(List<CategoryResponse> children) { this.children = children; }
    }
}
