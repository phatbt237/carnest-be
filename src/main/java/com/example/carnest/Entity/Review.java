package com.example.carnest.Entity;

import com.example.carnest.Enum.ReviewType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "review",
       uniqueConstraints = @UniqueConstraint(columnNames = {"order_id", "reviewer_id"}),
       indexes = {
           @Index(name = "idx_review_reviewer", columnList = "reviewer_id"),
           @Index(name = "idx_review_reviewed", columnList = "reviewed_id"),
           @Index(name = "idx_review_order", columnList = "order_id")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private User reviewer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_id", nullable = false)
    private User reviewed;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReviewType type;

    @Column(nullable = false)
    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "rating_accuracy")
    private Integer ratingAccuracy;

    @Column(name = "rating_shipping")
    private Integer ratingShipping;

    @Column(name = "rating_communication")
    private Integer ratingCommunication;

    @Column(columnDefinition = "TEXT")
    private String reply;

    @Column(name = "replied_at")
    private LocalDateTime repliedAt;

    @Column(name = "is_hidden")
    @Builder.Default
    private Boolean isHidden = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ===== RELATIONSHIPS =====

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ReviewImage> images = new ArrayList<>();
}
