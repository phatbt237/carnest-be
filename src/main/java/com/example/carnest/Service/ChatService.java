package com.example.carnest.Service;

import com.example.carnest.Entity.*;
import com.example.carnest.Enum.MessageType;
import com.example.carnest.Enum.TagType;
import com.example.carnest.Exception.BadRequestException;
import com.example.carnest.Exception.ResourceNotFoundException;
import com.example.carnest.Model.ShopDTO;
import com.example.carnest.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatService {

    @Autowired private ConversationRepository conversationRepository;
    @Autowired private MessageRepository messageRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private WantListRepository wantListRepository;
    @Autowired private SimpMessagingTemplate messagingTemplate;

    @Transactional
    public Map<String, Object> sendMessage(Long senderId, Long receiverId, String content,
                                            List<String> imageUrls, TagType tagType, Long tagId) {
        if (senderId.equals(receiverId)) throw new BadRequestException("Không thể nhắn cho chính mình");

        boolean hasContent = content != null && !content.isBlank();
        boolean hasImages = imageUrls != null && !imageUrls.isEmpty();
        if (!hasContent && !hasImages) {
            throw new BadRequestException("Tin nhắn phải có nội dung hoặc ảnh");
        }
        if (hasImages && imageUrls.size() > 10) {
            throw new BadRequestException("Tối đa 10 ảnh");
        }

        User sender = userRepository.findById(senderId).orElseThrow();
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", receiverId));

        Conversation conv = conversationRepository.findByUsers(senderId, receiverId)
                .orElseGet(() -> {
                    Conversation c = new Conversation();
                    c.setUser1(sender);
                    c.setUser2(receiver);
                    c.setUser1Unread(0);
                    c.setUser2Unread(0);
                    return conversationRepository.save(c);
                });

        applyTag(conv, tagType, tagId, senderId, receiverId);

        Message msg = new Message();
        msg.setConversation(conv);
        msg.setSender(sender);
        msg.setContent(hasContent ? content : "");
        msg.setMessageType(hasImages ? MessageType.IMAGE : MessageType.TEXT);
        msg.setIsRead(false);
        if (tagType != null && tagId != null) {
            msg.setTagType(tagType);
            msg.setTagId(tagId);
        }
        if (hasImages) {
            List<MessageAttachment> attachments = new ArrayList<>();
            for (int i = 0; i < imageUrls.size(); i++) {
                attachments.add(MessageAttachment.builder()
                        .message(msg).imageUrl(imageUrls.get(i)).sortOrder(i).build());
            }
            msg.setAttachments(attachments);
        }
        msg = messageRepository.save(msg);

        String preview = hasContent ? content : "[Hình ảnh]";
        conv.setLastMessageAt(LocalDateTime.now());
        conv.setLastMessagePreview(preview.length() > 100 ? preview.substring(0, 100) : preview);
        if (conv.getUser1().getId().equals(receiverId)) {
            conv.setUser1Unread(conv.getUser1Unread() + 1);
        } else {
            conv.setUser2Unread(conv.getUser2Unread() + 1);
        }
        conversationRepository.save(conv);

        Map<String, Object> payload = new HashMap<>();
        payload.put("conversationId", conv.getId());
        payload.put("messageId", msg.getId());
        payload.put("senderUsername", sender.getUsername());
        payload.put("content", msg.getContent());
        payload.put("imageUrls", hasImages ? imageUrls : List.of());
        payload.put("timestamp", msg.getCreatedAt().toString());
        if (msg.getTagType() != null) {
            payload.putAll(resolveTagInfo(msg.getTagType(), msg.getTagId()));
        }

        messagingTemplate.convertAndSend("/topic/chat/" + receiverId, payload);

        return payload;
    }

    private void applyTag(Conversation conv, TagType tagType, Long tagId, Long senderId, Long receiverId) {
        if (tagType == null || tagId == null) return;

        switch (tagType) {
            case PRODUCT -> {
                Product product = productRepository.findById(tagId)
                        .orElseThrow(() -> new ResourceNotFoundException("Product", "id", tagId));
                Long shopUserId = product.getShop().getUser().getId();
                if (!shopUserId.equals(senderId) && !shopUserId.equals(receiverId)) {
                    throw new BadRequestException("Sản phẩm không thuộc cuộc trò chuyện này");
                }
                conv.setProduct(product);
            }
            case ORDER -> {
                Order order = orderRepository.findById(tagId)
                        .orElseThrow(() -> new ResourceNotFoundException("Order", "id", tagId));
                Long buyerId = order.getBuyer().getId();
                Long shopUserId = order.getShop().getUser().getId();
                boolean matches = (buyerId.equals(senderId) && shopUserId.equals(receiverId))
                        || (buyerId.equals(receiverId) && shopUserId.equals(senderId));
                if (!matches) {
                    throw new BadRequestException("Đơn hàng không thuộc cuộc trò chuyện này");
                }
                conv.setOrder(order);
            }
            case WANT_LIST -> {
                WantList wantList = wantListRepository.findById(tagId)
                        .orElseThrow(() -> new ResourceNotFoundException("WantList", "id", tagId));
                Long ownerId = wantList.getUser().getId();
                if (!ownerId.equals(receiverId) || ownerId.equals(senderId)) {
                    throw new BadRequestException("Chỉ người bán mới có thể liên hệ về yêu cầu tìm xe này");
                }
                conv.setWantList(wantList);
            }
        }
    }

    private Map<String, Object> resolveTagInfo(TagType tagType, Long tagId) {
        Map<String, Object> m = new HashMap<>();
        if (tagType == null || tagId == null) return m;

        switch (tagType) {
            case PRODUCT -> productRepository.findById(tagId).ifPresent(p -> {
                m.put("tagType", TagType.PRODUCT);
                m.put("tagId", p.getId());
                m.put("tagTitle", p.getName());
                m.put("tagImageUrl", p.getImages().stream()
                        .filter(i -> Boolean.TRUE.equals(i.getIsPrimary())).findFirst()
                        .or(() -> p.getImages().stream().findFirst())
                        .map(ProductImage::getImageUrl).orElse(null));
            });
            case ORDER -> orderRepository.findById(tagId).ifPresent(o -> {
                m.put("tagType", TagType.ORDER);
                m.put("tagId", o.getId());
                m.put("tagTitle", o.getOrderCode());
                m.put("tagImageUrl", null);
            });
            case WANT_LIST -> wantListRepository.findById(tagId).ifPresent(w -> {
                m.put("tagType", TagType.WANT_LIST);
                m.put("tagId", w.getId());
                m.put("tagTitle", w.getTitle());
                m.put("tagImageUrl", null);
            });
        }
        return m;
    }

    private void addTagInfo(Map<String, Object> m, Conversation c) {
        if (c.getProduct() != null) {
            Product p = c.getProduct();
            m.put("tagType", TagType.PRODUCT);
            m.put("tagId", p.getId());
            m.put("tagTitle", p.getName());
            m.put("tagImageUrl", p.getImages().stream()
                    .filter(i -> Boolean.TRUE.equals(i.getIsPrimary())).findFirst()
                    .or(() -> p.getImages().stream().findFirst())
                    .map(ProductImage::getImageUrl).orElse(null));
        } else if (c.getOrder() != null) {
            Order o = c.getOrder();
            m.put("tagType", TagType.ORDER);
            m.put("tagId", o.getId());
            m.put("tagTitle", o.getOrderCode());
            m.put("tagImageUrl", null);
        } else if (c.getWantList() != null) {
            WantList w = c.getWantList();
            m.put("tagType", TagType.WANT_LIST);
            m.put("tagId", w.getId());
            m.put("tagTitle", w.getTitle());
            m.put("tagImageUrl", null);
        }
    }

    public ShopDTO.CursorPage<Map<String, Object>> getConversations(Long userId, String cursor, int size) {
        size = Math.min(Math.max(size, 1), 50);
        Long cursorId = (cursor != null && !cursor.isEmpty()) ? Long.parseLong(cursor) : null;

        List<Conversation> convs = conversationRepository.findByUserId(userId, cursorId, size + 1);
        boolean hasMore = convs.size() > size;
        if (hasMore) convs = convs.subList(0, size);

        List<Map<String, Object>> items = convs.stream().map(c -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", c.getId());
            User other = c.getUser1().getId().equals(userId) ? c.getUser2() : c.getUser1();
            m.put("otherUserId", other.getId());
            m.put("otherUsername", other.getUsername());
            m.put("otherAvatar", other.getAvatarUrl());
            m.put("lastMessage", c.getLastMessagePreview());
            m.put("lastMessageAt", c.getLastMessageAt());
            int unread = c.getUser1().getId().equals(userId) ? c.getUser1Unread() : c.getUser2Unread();
            m.put("unread", unread);
            addTagInfo(m, c);
            return m;
        }).collect(Collectors.toList());

        String nextCursor = hasMore && !convs.isEmpty() ? String.valueOf(convs.get(convs.size() - 1).getId()) : null;
        return new ShopDTO.CursorPage<>(items, nextCursor, hasMore, items.size(), conversationRepository.countByUserId(userId));
    }

    public ShopDTO.CursorPage<Map<String, Object>> getMessages(Long userId, Long conversationId, String cursor, int size) {
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation", "id", conversationId));
        if (!conv.getUser1().getId().equals(userId) && !conv.getUser2().getId().equals(userId)) {
            throw new BadRequestException("Bạn không thuộc cuộc trò chuyện này");
        }

        size = Math.min(Math.max(size, 1), 50);
        Long cursorId = (cursor != null && !cursor.isEmpty()) ? Long.parseLong(cursor) : null;

        List<Message> msgs = messageRepository.findByConversationId(conversationId, cursorId, size + 1);
        boolean hasMore = msgs.size() > size;
        if (hasMore) msgs = msgs.subList(0, size);

        List<Map<String, Object>> items = msgs.stream().map(m -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", m.getId());
            map.put("senderUsername", m.getSender().getUsername());
            map.put("content", m.getContent());
            map.put("type", m.getMessageType().name());
            map.put("isRead", m.getIsRead());
            map.put("createdAt", m.getCreatedAt());
            map.put("imageUrls", m.getAttachments().stream()
                    .sorted(Comparator.comparing(MessageAttachment::getSortOrder))
                    .map(MessageAttachment::getImageUrl).collect(Collectors.toList()));
            if (m.getTagType() != null) {
                map.putAll(resolveTagInfo(m.getTagType(), m.getTagId()));
            }
            return map;
        }).collect(Collectors.toList());

        String nextCursor = hasMore && !msgs.isEmpty() ? String.valueOf(msgs.get(msgs.size() - 1).getId()) : null;
        return new ShopDTO.CursorPage<>(items, nextCursor, hasMore, items.size(), messageRepository.countByConversationId(conversationId));
    }

    @Transactional
    public void markRead(Long userId, Long conversationId) {
        Conversation conv = conversationRepository.findById(conversationId).orElseThrow();
        if (conv.getUser1().getId().equals(userId)) conv.setUser1Unread(0);
        else conv.setUser2Unread(0);
        conversationRepository.save(conv);
    }
}