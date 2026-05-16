package com.example.carnest.Service;

import com.example.carnest.Entity.*;
import com.example.carnest.Enum.ProductStatus;
import com.example.carnest.Model.EventDTO;
import com.example.carnest.Enum.TradeStatus;
import com.example.carnest.Exception.BadRequestException;
import com.example.carnest.Exception.ResourceNotFoundException;
import com.example.carnest.Model.TradeDTO;
import com.example.carnest.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TradeService {

    @Autowired private TradeOfferRepository tradeOfferRepository;
    @Autowired private TradeOfferItemRepository tradeOfferItemRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private EventPublisher eventPublisher;

    @Transactional
    public TradeDTO.TradeResponse create(Long userId, TradeDTO.CreateRequest request) {
        Product target = productRepository.findById(request.getTargetProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getTargetProductId()));

        if (target.getStatus() != ProductStatus.ACTIVE) {
            throw new BadRequestException("Sản phẩm không còn bán");
        }
        if (target.getShop().getUser().getId().equals(userId)) {
            throw new BadRequestException("Không thể đổi xe với chính mình");
        }

        // Validate offer products
        for (Long pid : request.getOfferProductIds()) {
            Product p = productRepository.findById(pid)
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", pid));
            if (!p.getShop().getUser().getId().equals(userId)) {
                throw new BadRequestException("Sản phẩm '" + p.getName() + "' không thuộc về bạn");
            }
            if (p.getStatus() != ProductStatus.ACTIVE) {
                throw new BadRequestException("Sản phẩm '" + p.getName() + "' không ở trạng thái có thể đổi");
            }
        }

        User offerer = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        TradeOffer trade = new TradeOffer();
        trade.setOfferer(offerer);
        trade.setReceiver(target.getShop().getUser());
        trade.setTargetProduct(target);
        trade.setCashOffset(request.getCashOffset() != null ? request.getCashOffset() : BigDecimal.ZERO);
        trade.setMessage(request.getMessage());
        trade.setStatus(TradeStatus.PENDING);
        trade.setExpiresAt(LocalDateTime.now().plusHours(72));
        trade = tradeOfferRepository.save(trade);

        for (Long pid : request.getOfferProductIds()) {
            Product p = productRepository.findById(pid).orElseThrow();
            TradeOfferItem item = new TradeOfferItem();
            item.setTradeOffer(trade);
            item.setProduct(p);
            tradeOfferItemRepository.save(item);
        }

        eventPublisher.publishTradeEvent(buildTradeEvent(trade, "PROPOSED"));
        return toResponse(trade);
    }

    @Transactional
    public TradeDTO.TradeResponse accept(Long userId, Long tradeId) {
        TradeOffer trade = getTradeForReceiver(userId, tradeId);
        validatePending(trade);

        trade.setStatus(TradeStatus.ACCEPTED);
        trade.setRespondedAt(LocalDateTime.now());
        tradeOfferRepository.save(trade);
        eventPublisher.publishTradeEvent(buildTradeEvent(trade, "ACCEPTED"));
        return toResponse(trade);
    }

    @Transactional
    public TradeDTO.TradeResponse reject(Long userId, Long tradeId) {
        TradeOffer trade = getTradeForReceiver(userId, tradeId);
        validatePending(trade);

        trade.setStatus(TradeStatus.REJECTED);
        trade.setRespondedAt(LocalDateTime.now());
        tradeOfferRepository.save(trade);
        eventPublisher.publishTradeEvent(buildTradeEvent(trade, "REJECTED"));
        return toResponse(trade);
    }

    @Transactional
    public void cancel(Long userId, Long tradeId) {
        TradeOffer trade = tradeOfferRepository.findById(tradeId)
                .orElseThrow(() -> new ResourceNotFoundException("Trade", "id", tradeId));
        if (!trade.getOfferer().getId().equals(userId)) {
            throw new BadRequestException("Bạn không có quyền hủy đề xuất này");
        }
        if (trade.getStatus() != TradeStatus.PENDING) {
            throw new BadRequestException("Chỉ hủy được đề xuất đang chờ phản hồi");
        }
        trade.setStatus(TradeStatus.CANCELLED);
        tradeOfferRepository.save(trade);
        eventPublisher.publishTradeEvent(buildTradeEvent(trade, "CANCELLED"));
    }

    public List<TradeDTO.TradeResponse> getMyTrades(Long userId) {
        return tradeOfferRepository.findByOffererId(userId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public List<TradeDTO.TradeResponse> getReceivedTrades(Long userId) {
        return tradeOfferRepository.findByReceiverId(userId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    private EventDTO.TradeEvent buildTradeEvent(TradeOffer trade, String action) {
        EventDTO.TradeEvent event = new EventDTO.TradeEvent();
        event.setTradeId(trade.getId());
        event.setOffererId(trade.getOfferer().getId());
        event.setReceiverId(trade.getReceiver().getId());
        event.setOffererUsername(trade.getOfferer().getUsername());
        event.setReceiverUsername(trade.getReceiver().getUsername());
        event.setTargetProductName(trade.getTargetProduct().getName());
        event.setAction(action);
        return event;
    }

    private TradeOffer getTradeForReceiver(Long userId, Long tradeId) {
        TradeOffer trade = tradeOfferRepository.findById(tradeId)
                .orElseThrow(() -> new ResourceNotFoundException("Trade", "id", tradeId));
        if (!trade.getReceiver().getId().equals(userId)) {
            throw new BadRequestException("Bạn không có quyền thao tác đề xuất này");
        }
        return trade;
    }

    private void validatePending(TradeOffer trade) {
        if (trade.getStatus() != TradeStatus.PENDING) {
            throw new BadRequestException("Đề xuất không ở trạng thái chờ phản hồi");
        }
        if (trade.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Đề xuất đã hết hạn");
        }
    }

    private TradeDTO.TradeResponse toResponse(TradeOffer t) {
        TradeDTO.TradeResponse r = new TradeDTO.TradeResponse();
        r.setId(t.getId());
        r.setStatus(t.getStatus().name());
        r.setOffererUsername(t.getOfferer().getUsername());
        r.setReceiverUsername(t.getReceiver().getUsername());
        r.setTargetProductId(t.getTargetProduct().getId());
        r.setTargetProductName(t.getTargetProduct().getName());
        r.setCashOffset(t.getCashOffset());
        r.setMessage(t.getMessage());
        r.setExpiresAt(t.getExpiresAt());
        r.setCreatedAt(t.getCreatedAt());

        List<TradeOfferItem> items = tradeOfferItemRepository.findByTradeOfferId(t.getId());
        r.setOfferProducts(items.stream().map(i -> {
            TradeDTO.ProductInfo pi = new TradeDTO.ProductInfo();
            pi.setId(i.getProduct().getId());
            pi.setName(i.getProduct().getName());
            return pi;
        }).collect(Collectors.toList()));

        return r;
    }
}