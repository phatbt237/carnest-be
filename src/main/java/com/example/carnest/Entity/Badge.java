package com.example.carnest.Entity;

import com.example.carnest.Enum.BadgeTier;
import com.example.carnest.Enum.CriteriaType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "badge")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "icon_url", length = 500)
    private String iconUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "criteria_type", nullable = false, length = 30)
    private CriteriaType criteriaType;

    @Column(name = "criteria_value", nullable = false)
    private Integer criteriaValue;

    @Column(name = "criteria_extra", columnDefinition = "JSON")
    private String criteriaExtra;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private BadgeTier tier = BadgeTier.BRONZE;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}