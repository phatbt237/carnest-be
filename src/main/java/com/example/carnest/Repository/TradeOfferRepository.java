package com.example.carnest.Repository;

import com.example.carnest.Entity.TradeOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TradeOfferRepository extends JpaRepository<TradeOffer, Long> {
    List<TradeOffer> findByOffererId(Long offererId);
    List<TradeOffer> findByReceiverId(Long receiverId);
}