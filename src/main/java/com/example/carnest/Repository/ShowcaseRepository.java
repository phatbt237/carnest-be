package com.example.carnest.Repository;

import com.example.carnest.Entity.Showcase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ShowcaseRepository extends JpaRepository<Showcase, Long> {
    List<Showcase> findByUserIdOrderBySortOrderAsc(Long userId);
}