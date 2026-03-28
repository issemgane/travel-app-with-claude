package com.wanderlust.api.post;

import com.wanderlust.api.common.BaseEntity;
import com.wanderlust.api.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "travel_posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TravelPost extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PostCategory category;

    @Column(name = "cost_level")
    private Short costLevel;

    @Column(name = "best_season", length = 20)
    private String bestSeason;

    @Column(name = "duration_suggested", length = 50)
    private String durationSuggested;

    @Column(name = "accessibility_rating")
    private Short accessibilityRating;

    @Column(nullable = false, columnDefinition = "geography(Point,4326)")
    private Point location;

    @Column(name = "place_name", nullable = false)
    private String placeName;

    @Column(name = "country_code", nullable = false, length = 3)
    private String countryCode;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "tags", columnDefinition = "text[]")
    @Builder.Default
    private String[] tags = new String[]{};

    @Column(name = "likes_count", nullable = false)
    @Builder.Default
    private Integer likesCount = 0;

    @Column(name = "comments_count", nullable = false)
    @Builder.Default
    private Integer commentsCount = 0;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    @Builder.Default
    private List<PostMedia> media = new ArrayList<>();
}
