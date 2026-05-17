package com.example.carnest.Repository;

import com.example.carnest.Entity.Report;
import com.example.carnest.Enum.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByStatus(ReportStatus status);

    // ===== ADMIN REPORT =====
    @Query("SELECT COUNT(r) FROM Report r WHERE r.status = :status")
    Long countByStatus(@Param("status") ReportStatus status);

    @Query("SELECT r.reason, COUNT(r) FROM Report r GROUP BY r.reason ORDER BY 2 DESC")
    List<Object[]> countGroupByReason();

    @Query("SELECT r.targetType, COUNT(r) FROM Report r GROUP BY r.targetType ORDER BY 2 DESC")
    List<Object[]> countGroupByTargetType();
}