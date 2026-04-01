package com.example.carnest.API;

import com.example.carnest.Model.AuthDTO;
import com.example.carnest.Model.BrandDTO;
import com.example.carnest.Service.BrandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/brands")
@Tag(name = "Brand", description = "Quản lý hãng xe mô hình")
public class BrandController {

    private final BrandService brandService;

    @Autowired
    public BrandController(BrandService brandService) {
        this.brandService = brandService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Tạo hãng xe (Admin)")
    public ResponseEntity<AuthDTO.MessageResponse> create(@Valid @RequestBody BrandDTO.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AuthDTO.MessageResponse.builder()
                        .status(201).message("Tạo hãng thành công")
                        .data(brandService.create(request)).build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cập nhật hãng xe (Admin)")
    public ResponseEntity<AuthDTO.MessageResponse> update(
            @PathVariable Long id, @Valid @RequestBody BrandDTO.UpdateRequest request) {
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message("Cập nhật thành công")
                .data(brandService.update(id, request)).build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa hãng xe (Admin)")
    public ResponseEntity<AuthDTO.MessageResponse> delete(@PathVariable Long id) {
        brandService.delete(id);
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message("Xóa hãng thành công").build());
    }

    @GetMapping
    @Operation(summary = "Tất cả hãng xe (public)")
    public ResponseEntity<AuthDTO.MessageResponse> getAll() {
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message("Thành công")
                .data(brandService.getAll()).build());
    }

    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm hãng xe (public)")
    public ResponseEntity<AuthDTO.MessageResponse> search(@RequestParam String keyword) {
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message("Thành công")
                .data(brandService.search(keyword)).build());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết hãng xe (public)")
    public ResponseEntity<AuthDTO.MessageResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message("Thành công")
                .data(brandService.getById(id)).build());
    }
}
