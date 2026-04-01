package com.example.carnest.Repository;

import com.example.carnest.Entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Long> {

    // ===== TÌM THEO SLUG/USER — JOIN FETCH user để tránh N+1 =====
    @Query("SELECT s FROM Shop s JOIN FETCH s.user WHERE s.slug = :slug")
    Optional<Shop> findBySlugWithUser(@Param("slug") String slug);

    @Query("SELECT s FROM Shop s JOIN FETCH s.user WHERE s.user.id = :userId")
    Optional<Shop> findByUserIdWithUser(@Param("userId") Long userId);

    @Query("SELECT s FROM Shop s JOIN FETCH s.user WHERE s.id = :id")
    Optional<Shop> findByIdWithUser(@Param("id") Long id);

    Boolean existsByUserId(Long userId);
    Boolean existsBySlug(String slug);
    Boolean existsByShopName(String shopName);

    // ===== CURSOR-BASED: danh sách shop, sắp xếp theo followerCount DESC =====
    // Trang đầu (không có cursor)
    @Query("SELECT s FROM Shop s JOIN FETCH s.user " +
           "WHERE s.isActive = true " +
           "ORDER BY s.followerCount DESC, s.id DESC")
    List<Shop> findTopShops(@Param("limit") int limit);

    // Trang tiếp theo (có cursor: followerCount + id)
    @Query("SELECT s FROM Shop s JOIN FETCH s.user " +
           "WHERE s.isActive = true " +
           "AND (s.followerCount < :cursorFollower " +
           "     OR (s.followerCount = :cursorFollower AND s.id < :cursorId)) " +
           "ORDER BY s.followerCount DESC, s.id DESC")
    List<Shop> findShopsAfterCursor(
            @Param("cursorFollower") Integer cursorFollower,
            @Param("cursorId") Long cursorId,
            @Param("limit") int limit);

    // ===== CURSOR-BASED: sắp xếp theo rating DESC =====
    @Query("SELECT s FROM Shop s JOIN FETCH s.user " +
           "WHERE s.isActive = true " +
           "ORDER BY s.ratingAvg DESC, s.id DESC")
    List<Shop> findTopShopsByRating(@Param("limit") int limit);

    @Query("SELECT s FROM Shop s JOIN FETCH s.user " +
           "WHERE s.isActive = true " +
           "AND (s.ratingAvg < :cursorRating " +
           "     OR (s.ratingAvg = :cursorRating AND s.id < :cursorId)) " +
           "ORDER BY s.ratingAvg DESC, s.id DESC")
    List<Shop> findShopsByRatingAfterCursor(
            @Param("cursorRating") java.math.BigDecimal cursorRating,
            @Param("cursorId") Long cursorId,
            @Param("limit") int limit);

    // ===== CURSOR-BASED: sắp xếp theo mới nhất =====
    @Query("SELECT s FROM Shop s JOIN FETCH s.user " +
           "WHERE s.isActive = true " +
           "ORDER BY s.id DESC")
    List<Shop> findNewestShops(@Param("limit") int limit);

    @Query("SELECT s FROM Shop s JOIN FETCH s.user " +
           "WHERE s.isActive = true AND s.id < :cursorId " +
           "ORDER BY s.id DESC")
    List<Shop> findNewestShopsAfterCursor(
            @Param("cursorId") Long cursorId,
            @Param("limit") int limit);

    // ===== TÌM KIẾM — cursor-based =====
    @Query("SELECT s FROM Shop s JOIN FETCH s.user " +
           "WHERE s.isActive = true " +
           "AND LOWER(s.shopName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "ORDER BY s.followerCount DESC, s.id DESC")
    List<Shop> searchByName(
            @Param("keyword") String keyword,
            @Param("limit") int limit);

    @Query("SELECT s FROM Shop s JOIN FETCH s.user " +
           "WHERE s.isActive = true " +
           "AND LOWER(s.shopName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "AND (s.followerCount < :cursorFollower " +
           "     OR (s.followerCount = :cursorFollower AND s.id < :cursorId)) " +
           "ORDER BY s.followerCount DESC, s.id DESC")
    List<Shop> searchByNameAfterCursor(
            @Param("keyword") String keyword,
            @Param("cursorFollower") Integer cursorFollower,
            @Param("cursorId") Long cursorId,
            @Param("limit") int limit);

    // ===== ĐẾM tổng (cho metadata) =====
    @Query("SELECT COUNT(s) FROM Shop s WHERE s.isActive = true")
    Long countActiveShops();

    @Query("SELECT COUNT(s) FROM Shop s WHERE s.isActive = true AND LOWER(s.shopName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Long countByKeyword(@Param("keyword") String keyword);
}
