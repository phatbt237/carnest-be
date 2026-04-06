package com.example.carnest.Service;

import com.example.carnest.Entity.Notification;
import com.example.carnest.Entity.User;
import com.example.carnest.Enum.NotificationType;
import com.example.carnest.Model.ShopDTO;
import com.example.carnest.Repository.NotificationRepository;
import com.example.carnest.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    @Autowired private NotificationRepository notificationRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private SimpMessagingTemplate messagingTemplate;

    // Gửi notification + push qua WebSocket
    public void send(Long userId, NotificationType type, String title, String content,
                     String referenceType, Long referenceId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;

        Notification n = new Notification();
        n.setUser(user);
        n.setType(type);
        n.setTitle(title);
        n.setContent(content);
        n.setReferenceType(referenceType);
        n.setReferenceId(referenceId);
        n.setIsRead(false);
        n.setIsPushed(false);
        notificationRepository.save(n);

        // Push realtime
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", n.getId());
        payload.put("type", type.name());
        payload.put("title", title);
        payload.put("content", content);
        payload.put("createdAt", n.getCreatedAt().toString());
        messagingTemplate.convertAndSend("/topic/notifications/" + userId, payload);
    }

    public ShopDTO.CursorPage<Map<String, Object>> getNotifications(Long userId, String cursor, int size) {
        size = Math.min(Math.max(size, 1), 50);
        Long cursorId = (cursor != null && !cursor.isEmpty()) ? Long.parseLong(cursor) : null;

        List<Notification> notifs = notificationRepository.findByUserId(userId, cursorId, size + 1);
        boolean hasMore = notifs.size() > size;
        if (hasMore) notifs = notifs.subList(0, size);

        List<Map<String, Object>> items = notifs.stream().map(n -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", n.getId());
            m.put("type", n.getType().name());
            m.put("title", n.getTitle());
            m.put("content", n.getContent());
            m.put("referenceType", n.getReferenceType());
            m.put("referenceId", n.getReferenceId());
            m.put("isRead", n.getIsRead());
            m.put("createdAt", n.getCreatedAt());
            return m;
        }).collect(Collectors.toList());

        String nextCursor = hasMore && !notifs.isEmpty() ? String.valueOf(notifs.get(notifs.size() - 1).getId()) : null;
        return new ShopDTO.CursorPage<>(items, nextCursor, hasMore, items.size(), notificationRepository.countByUserId(userId));
    }

    public Long getUnreadCount(Long userId) {
        return notificationRepository.countUnread(userId);
    }

    @Transactional
    public void markAllRead(Long userId) {
        notificationRepository.markAllRead(userId);
    }
}