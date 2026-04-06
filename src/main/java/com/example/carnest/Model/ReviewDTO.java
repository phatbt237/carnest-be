package com.example.carnest.Model;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ReviewDTO {

    public static class CreateRequest {
        @NotNull private Long orderId;
        @NotNull @Min(1) @Max(5) private Integer rating;
        private String comment;
        @Min(1) @Max(5) private Integer ratingAccuracy;
        @Min(1) @Max(5) private Integer ratingShipping;
        @Min(1) @Max(5) private Integer ratingCommunication;
        private List<String> imageUrls;

        public CreateRequest() {}
        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }
        public Integer getRating() { return rating; }
        public void setRating(Integer rating) { this.rating = rating; }
        public String getComment() { return comment; }
        public void setComment(String comment) { this.comment = comment; }
        public Integer getRatingAccuracy() { return ratingAccuracy; }
        public void setRatingAccuracy(Integer ratingAccuracy) { this.ratingAccuracy = ratingAccuracy; }
        public Integer getRatingShipping() { return ratingShipping; }
        public void setRatingShipping(Integer ratingShipping) { this.ratingShipping = ratingShipping; }
        public Integer getRatingCommunication() { return ratingCommunication; }
        public void setRatingCommunication(Integer ratingCommunication) { this.ratingCommunication = ratingCommunication; }
        public List<String> getImageUrls() { return imageUrls; }
        public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
    }

    public static class ReplyRequest {
        @NotBlank private String reply;
        public ReplyRequest() {}
        public String getReply() { return reply; }
        public void setReply(String reply) { this.reply = reply; }
    }

    public static class ReviewResponse {
        private Long id;
        private Long orderId;
        private String type;
        private Integer rating;
        private String comment;
        private Integer ratingAccuracy;
        private Integer ratingShipping;
        private Integer ratingCommunication;
        private String reply;
        private LocalDateTime repliedAt;
        private String reviewerUsername;
        private String reviewerAvatar;
        private String reviewedUsername;
        private List<String> imageUrls;
        private LocalDateTime createdAt;

        public ReviewResponse() {}
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public Integer getRating() { return rating; }
        public void setRating(Integer rating) { this.rating = rating; }
        public String getComment() { return comment; }
        public void setComment(String comment) { this.comment = comment; }
        public Integer getRatingAccuracy() { return ratingAccuracy; }
        public void setRatingAccuracy(Integer ratingAccuracy) { this.ratingAccuracy = ratingAccuracy; }
        public Integer getRatingShipping() { return ratingShipping; }
        public void setRatingShipping(Integer ratingShipping) { this.ratingShipping = ratingShipping; }
        public Integer getRatingCommunication() { return ratingCommunication; }
        public void setRatingCommunication(Integer ratingCommunication) { this.ratingCommunication = ratingCommunication; }
        public String getReply() { return reply; }
        public void setReply(String reply) { this.reply = reply; }
        public LocalDateTime getRepliedAt() { return repliedAt; }
        public void setRepliedAt(LocalDateTime repliedAt) { this.repliedAt = repliedAt; }
        public String getReviewerUsername() { return reviewerUsername; }
        public void setReviewerUsername(String u) { this.reviewerUsername = u; }
        public String getReviewerAvatar() { return reviewerAvatar; }
        public void setReviewerAvatar(String a) { this.reviewerAvatar = a; }
        public String getReviewedUsername() { return reviewedUsername; }
        public void setReviewedUsername(String u) { this.reviewedUsername = u; }
        public List<String> getImageUrls() { return imageUrls; }
        public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }
}