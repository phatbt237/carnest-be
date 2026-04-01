package com.example.carnest.Repository;

import com.example.carnest.Entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {

    Optional<Brand> findBySlug(String slug);

    Boolean existsBySlug(String slug);

    Boolean existsByName(String name);

    @Query("SELECT b FROM Brand b WHERE b.isActive = true ORDER BY b.name ASC")
    List<Brand> findAllActive();

    @Query("SELECT b FROM Brand b WHERE b.isActive = true AND LOWER(b.name) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY b.name ASC")
    List<Brand> searchByName(@Param("keyword") String keyword);
}
