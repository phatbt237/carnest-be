package com.example.carnest.API;

import com.example.carnest.Model.AuthDTO;
import com.example.carnest.Model.CategoryDTO;
import com.example.carnest.Service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "Category", description = "Quản lý danh mục xe mô hình")
public class CategoryController {

    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Tạo danh mục (Admin)")
    public ResponseEntity<AuthDTO.MessageResponse> create(@Valid @RequestBody CategoryDTO.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AuthDTO.MessageResponse.builder()
                        .status(201).message("Tạo danh mục thành công")
                        .data(categoryService.create(request)).build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cập nhật danh mục (Admin)")
    public ResponseEntity<AuthDTO.MessageResponse> update(
            @PathVariable Long id, @Valid @RequestBody CategoryDTO.UpdateRequest request) {
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message("Cập nhật thành công")
                .data(categoryService.update(id, request)).build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa danh mục (Admin)")
    public ResponseEntity<AuthDTO.MessageResponse> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message("Xóa danh mục thành công").build());
    }

    @GetMapping("/tree")
    @Operation(summary = "Danh mục dạng cây (public)")
    public ResponseEntity<AuthDTO.MessageResponse> getTree() {
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message("Thành công")
                .data(categoryService.getCategoryTree()).build());
    }

    @GetMapping
    @Operation(summary = "Tất cả danh mục (public)")
    public ResponseEntity<AuthDTO.MessageResponse> getAll() {
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message("Thành công")
                .data(categoryService.getAll()).build());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết danh mục (public)")
    public ResponseEntity<AuthDTO.MessageResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message("Thành công")
                .data(categoryService.getById(id)).build());
    }
}
