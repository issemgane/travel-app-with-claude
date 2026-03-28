package com.wanderlust.api.user;

import com.wanderlust.api.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "travel_style")
    private TravelStyle travelStyle;

    @Column(name = "countries_visited_count", nullable = false)
    @Builder.Default
    private Integer countriesVisitedCount = 0;

    @Column(name = "current_location", columnDefinition = "geography(Point,4326)")
    private Point currentLocation;
}
