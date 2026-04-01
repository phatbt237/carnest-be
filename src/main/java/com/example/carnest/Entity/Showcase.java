package com.example.carnest.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "showcase")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Showcase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    @Column(name = "is_public")
    @Builder.Default
    private Boolean isPublic = true;

    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(name = "item_count")
    @Builder.Default
    private Integer itemCount = 0;

    @Column(name = "view_count")
    @Builder.Default
    private Integer viewCount = 0;

    @Column(name = "like_count")
    @Builder.Default
    private Integer likeCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ===== RELATIONSHIPS =====

    @OneToMany(mappedBy = "showcase", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ShowcaseItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "showcase", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ShowcaseLike> likes = new ArrayList<>();
}
