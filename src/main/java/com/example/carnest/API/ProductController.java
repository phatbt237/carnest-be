package com.example.carnest.API;

import com.example.carnest.Config.CustomUserDetails;
import com.example.carnest.Enum.ProductCondition;
import com.example.carnest.Model.AuthDTO;
import com.example.carnest.Model.ProductDTO;
import com.example.carnest.Model.ShopDTO;
import com.example.carnest.Service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Product", description = "Quản lý sản phẩm xe mô hình")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @Operation(summary = "Đăng bán sản phẩm", description = "Cần có shop trước")
    public ResponseEntity<AuthDTO.MessageResponse> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ProductDTO.CreateRequest request) {

        ProductDTO.ProductResponse product = productService.create(userDetails.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AuthDTO.MessageResponse.builder()
                        .status(201).message("Đăng bán thành công").data(product).build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật sản phẩm")
    public ResponseEntity<AuthDTO.MessageResponse> update(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody ProductDTO.CreateRequest request) {

        ProductDTO.ProductResponse product = productService.update(userDetails.getUserId(), id, request);
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message("Cập nhật thành công").data(product).build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa sản phẩm (soft delete)")
    public ResponseEntity<AuthDTO.MessageResponse> delete(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {

        productService.delete(userDetails.getUserId(), id);
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message("Xóa sản phẩm thành công").build());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết sản phẩm (public)")
    public ResponseEntity<AuthDTO.MessageResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message("Thành công")
                .data(productService.getById(id)).build());
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Chi tiết sản phẩm theo slug (public)")
    public ResponseEntity<AuthDTO.MessageResponse> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message("Thành công")
                .data(productService.getBySlug(slug)).build());
    }

    @GetMapping
    @Operation(summary = "Danh sách & lọc sản phẩm (cursor-based, public)",
               description = "sortBy: newest (default), price_asc, price_desc. Hỗ trợ lọc theo shop, category, brand, scale, condition, giá, keyword.")
    public ResponseEntity<AuthDTO.MessageResponse> getProducts(
            @Parameter(description = "newest | price_asc | price_desc")
            @RequestParam(defaultValue = "newest") String sortBy,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long shopId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) String scale,
            @RequestParam(required = false) ProductCondition condition,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String keyword) {

        ShopDTO.CursorPage<ProductDTO.ProductSummary> page = productService.getProducts(
                sortBy, cursor, size, shopId, categoryId, brandId, scale, condition, minPrice, maxPrice, keyword);

        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message("Thành công").data(page).build());
    }

    @GetMapping("/shop/{shopId}")
    @Operation(summary = "Sản phẩm theo shop (public)")
    public ResponseEntity<AuthDTO.MessageResponse> getByShop(
            @PathVariable Long shopId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size) {

        ShopDTO.CursorPage<ProductDTO.ProductSummary> page = productService.getByShop(shopId, cursor, size);

        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message("Thành công").data(page).build());
    }
}
