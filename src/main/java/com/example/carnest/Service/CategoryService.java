package com.example.carnest.Service;

import com.example.carnest.Entity.Category;
import com.example.carnest.Exception.BadRequestException;
import com.example.carnest.Exception.ResourceNotFoundException;
import com.example.carnest.Model.CategoryDTO;
import com.example.carnest.Repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public CategoryDTO.CategoryResponse create(CategoryDTO.CreateRequest request) {
        if (categoryRepository.existsByName(request.getName().trim())) {
            throw new BadRequestException("Tên danh mục đã tồn tại");
        }

        Category category = new Category();
        category.setName(request.getName().trim());
        category.setSlug(generateSlug(request.getName()));
        category.setIconUrl(request.getIconUrl());
        category.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        category.setIsActive(true);

        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getParentId()));
            category.setParent(parent);
        }

        category = categoryRepository.save(category);
        return toResponse(category);
    }

    @Transactional
    public CategoryDTO.CategoryResponse update(Long id, CategoryDTO.UpdateRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        if (request.getName() != null && !request.getName().trim().equals(category.getName())) {
            if (categoryRepository.existsByName(request.getName().trim())) {
                throw new BadRequestException("Tên danh mục đã tồn tại");
            }
            category.setName(request.getName().trim());
            category.setSlug(generateSlug(request.getName()));
        }
        if (request.getIconUrl() != null) category.setIconUrl(request.getIconUrl());
        if (request.getSortOrder() != null) category.setSortOrder(request.getSortOrder());
        if (request.getIsActive() != null) category.setIsActive(request.getIsActive());

        category = categoryRepository.save(category);
        return toResponse(category);
    }

    // Danh sách dạng cây (root + children)
    public List<CategoryDTO.CategoryResponse> getCategoryTree() {
        List<Category> roots = categoryRepository.findRootCategories();
        return roots.stream().map(root -> {
            CategoryDTO.CategoryResponse response = toResponse(root);
            List<Category> children = categoryRepository.findByParentId(root.getId());
            response.setChildren(children.stream().map(this::toResponse).collect(Collectors.toList()));
            return response;
        }).collect(Collectors.toList());
    }

    // Tất cả danh mục (flat)
    public List<CategoryDTO.CategoryResponse> getAll() {
        return categoryRepository.findAllActive().stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public CategoryDTO.CategoryResponse getById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        CategoryDTO.CategoryResponse response = toResponse(category);
        List<Category> children = categoryRepository.findByParentId(id);
        response.setChildren(children.stream().map(this::toResponse).collect(Collectors.toList()));
        return response;
    }

    @Transactional
    public void delete(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        category.setIsActive(false);
        categoryRepository.save(category);
    }

    private CategoryDTO.CategoryResponse toResponse(Category c) {
        CategoryDTO.CategoryResponse r = new CategoryDTO.CategoryResponse();
        r.setId(c.getId());
        r.setName(c.getName());
        r.setSlug(c.getSlug());
        r.setIconUrl(c.getIconUrl());
        r.setSortOrder(c.getSortOrder());
        r.setIsActive(c.getIsActive());
        if (c.getParent() != null) {
            r.setParentId(c.getParent().getId());
            r.setParentName(c.getParent().getName());
        }
        return r;
    }

    private String generateSlug(String name) {
        String slug = name.toLowerCase().trim()
                .replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a")
                .replaceAll("[èéẹẻẽêềếệểễ]", "e")
                .replaceAll("[ìíịỉĩ]", "i")
                .replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o")
                .replaceAll("[ùúụủũưừứựửữ]", "u")
                .replaceAll("[ỳýỵỷỹ]", "y")
                .replaceAll("[đ]", "d")
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("[\\s]+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
        String base = slug;
        int c = 1;
        while (categoryRepository.existsBySlug(slug)) { slug = base + "-" + c++; }
        return slug;
    }
}
