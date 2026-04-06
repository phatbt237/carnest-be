package com.example.carnest.Repository;

import com.example.carnest.Entity.TradeOfferItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TradeOfferItemRepository extends JpaRepository<TradeOfferItem, Long> {
    List<TradeOfferItem> findByTradeOfferId(Long tradeOfferId);
}