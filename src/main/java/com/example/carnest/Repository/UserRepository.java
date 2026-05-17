package com.example.carnest.Repository;

import com.example.carnest.Entity.User;
import com.example.carnest.Enum.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Boolean existsByUsername(String username);

    // ===== ADMIN REPORT =====
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    Long countByRole(@Param("role") Role role);

    @Query("SELECT COUNT(u) FROM User u WHERE u.isBanned = true")
    Long countBanned();

    @Query("SELECT COUNT(u) FROM User u WHERE u.isSeller = true")
    Long countSellers();

    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt BETWEEN :from AND :to")
    Long countNewUsers(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // Tăng trưởng user theo ngày
    @Query("SELECT FUNCTION('DATE_FORMAT', u.createdAt, '%Y-%m-%d'), COUNT(u) " +
           "FROM User u WHERE u.createdAt BETWEEN :from AND :to " +
           "GROUP BY FUNCTION('DATE_FORMAT', u.createdAt, '%Y-%m-%d') ORDER BY 1 ASC")
    List<Object[]> userGrowthByDay(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // Tăng trưởng user theo tháng
    @Query("SELECT FUNCTION('DATE_FORMAT', u.createdAt, '%Y-%m'), COUNT(u) " +
           "FROM User u WHERE u.createdAt BETWEEN :from AND :to " +
           "GROUP BY FUNCTION('DATE_FORMAT', u.createdAt, '%Y-%m') ORDER BY 1 ASC")
    List<Object[]> userGrowthByMonth(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
