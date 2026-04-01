package com.example.carnest.Repository;

import com.example.carnest.Entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    List<ProductImage> findByProductIdOrderBySortOrderAsc(Long productId);

    // Batch: lấy ảnh primary cho danh sách product
    @Query("SELECT pi FROM ProductImage pi WHERE pi.isPrimary = true AND pi.product.id IN :productIds")
    List<ProductImage> findPrimaryByProductIds(@Param("productIds") List<Long> productIds);

    // Đếm ảnh theo product
    Long countByProductId(Long productId);

    void deleteByProductId(Long productId);
}
