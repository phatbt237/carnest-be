package com.example.carnest.API;

import com.example.carnest.Config.CustomUserDetails;
import com.example.carnest.Entity.*;
import com.example.carnest.Enum.*;
import com.example.carnest.Exception.BadRequestException;
import com.example.carnest.Exception.ResourceNotFoundException;
import com.example.carnest.Model.AuthDTO;
import com.example.carnest.Repository.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/trades")
@Tag(name = "Trade", description = "Đổi xe")
public class TradeController {

    @Autowired private TradeOfferRepository tradeOfferRepository;
    @Autowired private TradeOfferItemRepository tradeOfferItemRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private UserRepository userRepository;

    @PostMapping
    @Operation(summary = "Đề xuất đổi xe")
    @Transactional
    public ResponseEntity<AuthDTO.MessageResponse> create(
            @AuthenticationPrincipal CustomUserDetails u, @RequestBody Map<String, Object> body) {
        Long targetProductId = Long.parseLong(body.get("targetProductId").toString());
        List<Long> offerProductIds = ((List<?>) body.get("offerProductIds")).stream()
                .map(o -> Long.parseLong(o.toString())).collect(Collectors.toList());
        BigDecimal cashOffset = body.get("cashOffset") != null ? new BigDecimal(body.get("cashOffset").toString()) : BigDecimal.ZERO;

        Product target = productRepository.findById(targetProductId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", targetProductId));
        if (target.getShop().getUser().getId().equals(u.getUserId()))
            throw new BadRequestException("Không thể đổi xe với chính mình");

        User offerer = userRepository.findById(u.getUserId()).orElseThrow();

        TradeOffer trade = new TradeOffer();
        trade.setOfferer(offerer);
        trade.setReceiver(target.getShop().getUser());
        trade.setTargetProduct(target);
        trade.setCashOffset(cashOffset);
        trade.setMessage((String) body.get("message"));
        trade.setStatus(TradeStatus.PENDING);
        trade.setExpiresAt(LocalDateTime.now().plusHours(72));
        trade = tradeOfferRepository.save(trade);

        for (Long pid : offerProductIds) {
            Product p = productRepository.findById(pid)
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", pid));
            TradeOfferItem item = new TradeOfferItem();
            item.setTradeOffer(trade);
            item.setProduct(p);
            tradeOfferItemRepository.save(item);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(AuthDTO.MessageResponse.builder()
                .status(201).message("Đề xuất đổi xe thành công").data(toMap(trade)).build());
    }

    @PutMapping("/{id}/accept")
    @Operation(summary = "Chấp nhận đổi xe")
    @Transactional
    public ResponseEntity<AuthDTO.MessageResponse> accept(
            @AuthenticationPrincipal CustomUserDetails u, @PathVariable Long id) {
        TradeOffer trade = tradeOfferRepository.findById(id).orElseThrow();
        if (!trade.getReceiver().getId().equals(u.getUserId())) throw new BadRequestException("Không có quyền");
        if (trade.getStatus() != TradeStatus.PENDING) throw new BadRequestException("Không ở trạng thái chờ");
        trade.setStatus(TradeStatus.ACCEPTED);
        trade.setRespondedAt(LocalDateTime.now());
        tradeOfferRepository.save(trade);
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder().status(200).message("Đã chấp nhận").data(toMap(trade)).build());
    }

    @PutMapping("/{id}/reject")
    @Operation(summary = "Từ chối đổi xe")
    @Transactional
    public ResponseEntity<AuthDTO.MessageResponse> reject(
            @AuthenticationPrincipal CustomUserDetails u, @PathVariable Long id) {
        TradeOffer trade = tradeOfferRepository.findById(id).orElseThrow();
        if (!trade.getReceiver().getId().equals(u.getUserId())) throw new BadRequestException("Không có quyền");
        trade.setStatus(TradeStatus.REJECTED);
        trade.setRespondedAt(LocalDateTime.now());
        tradeOfferRepository.save(trade);
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder().status(200).message("Đã từ chối").data(toMap(trade)).build());
    }

    @GetMapping("/my")
    @Operation(summary = "Trade tôi đề xuất")
    public ResponseEntity<AuthDTO.MessageResponse> my(@AuthenticationPrincipal CustomUserDetails u) {
        List<TradeOffer> trades = tradeOfferRepository.findByOffererId(u.getUserId());
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder().status(200).message("Thành công")
                .data(trades.stream().map(this::toMap).collect(Collectors.toList())).build());
    }

    @GetMapping("/received")
    @Operation(summary = "Trade tôi nhận được")
    public ResponseEntity<AuthDTO.MessageResponse> received(@AuthenticationPrincipal CustomUserDetails u) {
        List<TradeOffer> trades = tradeOfferRepository.findByReceiverId(u.getUserId());
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder().status(200).message("Thành công")
                .data(trades.stream().map(this::toMap).collect(Collectors.toList())).build());
    }

    private Map<String, Object> toMap(TradeOffer t) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", t.getId()); m.put("status", t.getStatus().name());
        m.put("offererUsername", t.getOfferer().getUsername());
        m.put("receiverUsername", t.getReceiver().getUsername());
        m.put("targetProductId", t.getTargetProduct().getId());
        m.put("targetProductName", t.getTargetProduct().getName());
        m.put("cashOffset", t.getCashOffset()); m.put("message", t.getMessage());
        m.put("expiresAt", t.getExpiresAt()); m.put("createdAt", t.getCreatedAt());
        List<TradeOfferItem> items = tradeOfferItemRepository.findByTradeOfferId(t.getId());
        m.put("offerProducts", items.stream().map(i -> {
            Map<String, Object> pm = new HashMap<>();
            pm.put("id", i.getProduct().getId()); pm.put("name", i.getProduct().getName());
            return pm;
        }).collect(Collectors.toList()));
        return m;
    }
}