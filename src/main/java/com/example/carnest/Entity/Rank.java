package com.example.carnest.Entity;

import com.example.carnest.Enum.RankType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ranks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rank {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RankType type;

    @Column(name = "min_point", nullable = false)
    private Integer minPoint;

    @Column(name = "max_point")
    private Integer maxPoint;

    @Column(name = "icon_url", length = 500)
    private String iconUrl;

    @Column(name = "color_hex", length = 7)
    private String colorHex;

    @Column(columnDefinition = "JSON")
    private String benefits;
}