package com.example.carnest.Repository;

import com.example.carnest.Entity.Product;
import com.example.carnest.Enum.ProductCondition;
import com.example.carnest.Enum.ProductStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // ===== CHI TIẾT SẢN PHẨM — fetch tất cả quan hệ 1 lần =====
    @Query("SELECT p FROM Product p " +
           "JOIN FETCH p.shop s JOIN FETCH s.user " +
           "LEFT JOIN FETCH p.category LEFT JOIN FETCH p.brand " +
           "WHERE p.id = :id")
    Optional<Product> findByIdFull(@Param("id") Long id);

    @Query("SELECT p FROM Product p " +
           "JOIN FETCH p.shop s JOIN FETCH s.user " +
           "LEFT JOIN FETCH p.category LEFT JOIN FETCH p.brand " +
           "WHERE p.slug = :slug")
    Optional<Product> findBySlugFull(@Param("slug") String slug);

    Boolean existsBySlug(String slug);

    // ===== DANH SÁCH SẢN PHẨM — cursor-based, mới nhất =====
    @Query("SELECT p FROM Product p " +
           "JOIN FETCH p.shop s JOIN FETCH s.user " +
           "LEFT JOIN FETCH p.brand " +
           "WHERE p.status = 'ACTIVE' " +
           "ORDER BY p.id DESC")
    List<Product> findNewest(@Param("limit") int limit);

    @Query("SELECT p FROM Product p " +
           "JOIN FETCH p.shop s JOIN FETCH s.user " +
           "LEFT JOIN FETCH p.brand " +
           "WHERE p.status = 'ACTIVE' AND p.id < :cursorId " +
           "ORDER BY p.id DESC")
    List<Product> findNewestAfterCursor(@Param("cursorId") Long cursorId, @Param("limit") int limit);

    // ===== DANH SÁCH THEO GIÁ TĂNG DẦN =====
    @Query("SELECT p FROM Product p " +
           "JOIN FETCH p.shop s JOIN FETCH s.user " +
           "LEFT JOIN FETCH p.brand " +
           "WHERE p.status = 'ACTIVE' " +
           "ORDER BY p.price ASC, p.id ASC")
    List<Product> findByPriceAsc(@Param("limit") int limit);

    @Query("SELECT p FROM Product p " +
           "JOIN FETCH p.shop s JOIN FETCH s.user " +
           "LEFT JOIN FETCH p.brand " +
           "WHERE p.status = 'ACTIVE' " +
           "AND (p.price > :cursorPrice OR (p.price = :cursorPrice AND p.id > :cursorId)) " +
           "ORDER BY p.price ASC, p.id ASC")
    List<Product> findByPriceAscAfterCursor(
            @Param("cursorPrice") BigDecimal cursorPrice,
            @Param("cursorId") Long cursorId,
            @Param("limit") int limit);

    // ===== DANH SÁCH THEO GIÁ GIẢM DẦN =====
    @Query("SELECT p FROM Product p " +
           "JOIN FETCH p.shop s JOIN FETCH s.user " +
           "LEFT JOIN FETCH p.brand " +
           "WHERE p.status = 'ACTIVE' " +
           "ORDER BY p.price DESC, p.id DESC")
    List<Product> findByPriceDesc(@Param("limit") int limit);

    @Query("SELECT p FROM Product p " +
           "JOIN FETCH p.shop s JOIN FETCH s.user " +
           "LEFT JOIN FETCH p.brand " +
           "WHERE p.status = 'ACTIVE' " +
           "AND (p.price < :cursorPrice OR (p.price = :cursorPrice AND p.id < :cursorId)) " +
           "ORDER BY p.price DESC, p.id DESC")
    List<Product> findByPriceDescAfterCursor(
            @Param("cursorPrice") BigDecimal cursorPrice,
            @Param("cursorId") Long cursorId,
            @Param("limit") int limit);

    // ===== LỌC NÂNG CAO — nhiều tiêu chí =====
    @Query("SELECT p FROM Product p " +
           "JOIN FETCH p.shop s JOIN FETCH s.user " +
           "LEFT JOIN FETCH p.brand " +
           "WHERE p.status = 'ACTIVE' " +
           "AND (:shopId IS NULL OR p.shop.id = :shopId) " +
           "AND (:categoryId IS NULL OR p.category.id = :categoryId) " +
           "AND (:brandId IS NULL OR p.brand.id = :brandId) " +
           "AND (:scale IS NULL OR p.scale = :scale) " +
           "AND (:condition IS NULL OR p.condition = :condition) " +
           "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
           "AND (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(p.carBrand) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(p.carModel) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:cursorId IS NULL OR p.id < :cursorId) " +
           "ORDER BY p.id DESC")
    List<Product> filterProducts(
            @Param("shopId") Long shopId,
            @Param("categoryId") Long categoryId,
            @Param("brandId") Long brandId,
            @Param("scale") String scale,
            @Param("condition") ProductCondition condition,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("keyword") String keyword,
            @Param("cursorId") Long cursorId,
            @Param("limit") int limit);

    // ===== SẢN PHẨM THEO SHOP =====
    @Query("SELECT p FROM Product p " +
           "LEFT JOIN FETCH p.brand " +
           "WHERE p.shop.id = :shopId AND p.status = 'ACTIVE' " +
           "AND (:cursorId IS NULL OR p.id < :cursorId) " +
           "ORDER BY p.id DESC")
    List<Product> findByShopId(
            @Param("shopId") Long shopId,
            @Param("cursorId") Long cursorId,
            @Param("limit") int limit);

    // ===== ĐẾM =====
    @Query("SELECT COUNT(p) FROM Product p WHERE p.status = 'ACTIVE'")
    Long countActive();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.shop.id = :shopId AND p.status = 'ACTIVE'")
    Long countByShopId(@Param("shopId") Long shopId);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.status = 'ACTIVE' " +
           "AND (:shopId IS NULL OR p.shop.id = :shopId) " +
           "AND (:categoryId IS NULL OR p.category.id = :categoryId) " +
           "AND (:brandId IS NULL OR p.brand.id = :brandId) " +
           "AND (:scale IS NULL OR p.scale = :scale) " +
           "AND (:condition IS NULL OR p.condition = :condition) " +
           "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
           "AND (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(p.carBrand) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(p.carModel) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Long countFiltered(
            @Param("shopId") Long shopId,
            @Param("categoryId") Long categoryId,
            @Param("brandId") Long brandId,
            @Param("scale") String scale,
            @Param("condition") ProductCondition condition,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("keyword") String keyword);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);
}
