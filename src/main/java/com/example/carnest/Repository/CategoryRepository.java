package com.example.carnest.Repository;

import com.example.carnest.Entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findBySlug(String slug);

    Boolean existsBySlug(String slug);

    Boolean existsByName(String name);

    // Danh mục gốc (parent = null)
    @Query("SELECT c FROM Category c WHERE c.parent IS NULL AND c.isActive = true ORDER BY c.sortOrder ASC")
    List<Category> findRootCategories();

    // Danh mục con theo parent
    @Query("SELECT c FROM Category c WHERE c.parent.id = :parentId AND c.isActive = true ORDER BY c.sortOrder ASC")
    List<Category> findByParentId(@Param("parentId") Long parentId);

    // Tất cả danh mục active
    @Query("SELECT c FROM Category c WHERE c.isActive = true ORDER BY c.sortOrder ASC")
    List<Category> findAllActive();
}
