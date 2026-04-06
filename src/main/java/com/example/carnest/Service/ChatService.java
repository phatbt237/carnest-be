package com.example.carnest.Service;

import com.example.carnest.Entity.*;
import com.example.carnest.Enum.MessageType;
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
    @Autowired private SimpMessagingTemplate messagingTemplate;

    @Transactional
    public Map<String, Object> sendMessage(Long senderId, Long receiverId, String content) {
        if (senderId.equals(receiverId)) throw new BadRequestException("Không thể nhắn cho chính mình");

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

        Message msg = new Message();
        msg.setConversation(conv);
        msg.setSender(sender);
        msg.setContent(content);
        msg.setMessageType(MessageType.TEXT);
        msg.setIsRead(false);
        msg = messageRepository.save(msg);

        conv.setLastMessageAt(LocalDateTime.now());
        conv.setLastMessagePreview(content.length() > 100 ? content.substring(0, 100) : content);
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
        payload.put("content", content);
        payload.put("timestamp", msg.getCreatedAt().toString());

        messagingTemplate.convertAndSend("/topic/chat/" + receiverId, payload);

        return payload;
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
            m.put("otherUsername", other.getUsername());
            m.put("otherAvatar", other.getAvatarUrl());
            m.put("lastMessage", c.getLastMessagePreview());
            m.put("lastMessageAt", c.getLastMessageAt());
            int unread = c.getUser1().getId().equals(userId) ? c.getUser1Unread() : c.getUser2Unread();
            m.put("unread", unread);
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