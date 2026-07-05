package com.example.carnest.Model;

import java.time.LocalDateTime;
import java.util.List;

public class ChatDTO {

    public static class SendMessageRequest {
        private String content;
        private List<String> imageUrls;
        private String tagType;
        private Long tagId;

        public SendMessageRequest() {}
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public List<String> getImageUrls() { return imageUrls; }
        public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
        public String getTagType() { return tagType; }
        public void setTagType(String tagType) { this.tagType = tagType; }
        public Long getTagId() { return tagId; }
        public void setTagId(Long tagId) { this.tagId = tagId; }
    }

    public static class ConversationResponse {
        private Long id;
        private String otherUsername;
        private String otherAvatar;
        private String lastMessage;
        private LocalDateTime lastMessageAt;
        private Integer unread;

        public ConversationResponse() {}
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getOtherUsername() { return otherUsername; }
        public void setOtherUsername(String otherUsername) { this.otherUsername = otherUsername; }
        public String getOtherAvatar() { return otherAvatar; }
        public void setOtherAvatar(String otherAvatar) { this.otherAvatar = otherAvatar; }
        public String getLastMessage() { return lastMessage; }
        public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
        public LocalDateTime getLastMessageAt() { return lastMessageAt; }
        public void setLastMessageAt(LocalDateTime lastMessageAt) { this.lastMessageAt = lastMessageAt; }
        public Integer getUnread() { return unread; }
        public void setUnread(Integer unread) { this.unread = unread; }
    }

    public static class MessageResponse {
        private Long id;
        private String senderUsername;
        private String content;
        private String type;
        private Boolean isRead;
        private LocalDateTime createdAt;

        public MessageResponse() {}
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getSenderUsername() { return senderUsername; }
        public void setSenderUsername(String senderUsername) { this.senderUsername = senderUsername; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public Boolean getIsRead() { return isRead; }
        public void setIsRead(Boolean isRead) { this.isRead = isRead; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }
}