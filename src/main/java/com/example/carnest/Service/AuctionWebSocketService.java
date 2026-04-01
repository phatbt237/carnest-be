package com.example.carnest.Service;

import com.example.carnest.Entity.Auction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuctionWebSocketService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // Gửi cập nhật auction đến tất cả client đang xem
    public void sendUpdate(Auction auction) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("auctionId", auction.getId());
        payload.put("currentPrice", auction.getCurrentPrice());
        payload.put("totalBids", auction.getTotalBids());
        payload.put("endTime", auction.getEndTime().toString());
        payload.put("status", auction.getStatus().name());
        payload.put("extendedCount", auction.getExtendedCount());

        if (auction.getWinner() != null) {
            payload.put("winnerUsername", auction.getWinner().getUsername());
        }

        // Gửi đến topic /topic/auction/{id}
        messagingTemplate.convertAndSend(
                "/topic/auction/" + auction.getId(), payload);
    }

    // Gửi thông báo bid mới
    public void sendNewBid(Long auctionId, String bidderUsername,
                           BigDecimal bidAmount, boolean isAutoBid) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "NEW_BID");
        payload.put("auctionId", auctionId);
        payload.put("bidderUsername", bidderUsername);
        payload.put("bidAmount", bidAmount);
        payload.put("isAutoBid", isAutoBid);
        payload.put("timestamp", LocalDateTime.now().toString());

        messagingTemplate.convertAndSend(
                "/topic/auction/" + auctionId, payload);
    }

    // Gửi thông báo auction kết thúc
    public void sendAuctionEnded(Auction auction) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "AUCTION_ENDED");
        payload.put("auctionId", auction.getId());
        payload.put("status", auction.getStatus().name());
        payload.put("finalPrice", auction.getCurrentPrice());

        if (auction.getWinner() != null) {
            payload.put("winnerUsername", auction.getWinner().getUsername());
        }

        messagingTemplate.convertAndSend(
                "/topic/auction/" + auction.getId(), payload);
    }
}