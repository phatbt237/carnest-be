package com.example.carnest.Repository;

import com.example.carnest.Entity.ShowcaseItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ShowcaseItemRepository extends JpaRepository<ShowcaseItem, Long> {
    List<ShowcaseItem> findByShowcaseIdOrderBySortOrderAsc(Long showcaseId);
}