package com.example.carnest.Repository;

import com.example.carnest.Entity.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {

    @Query("SELECT wt FROM WalletTransaction wt " +
           "WHERE wt.wallet.id = :walletId " +
           "AND (:cursorId IS NULL OR wt.id < :cursorId) " +
           "ORDER BY wt.id DESC")
    List<WalletTransaction> findByWalletId(
            @Param("walletId") Long walletId,
            @Param("cursorId") Long cursorId,
            @Param("limit") int limit);

    Long countByWalletId(Long walletId);
}
