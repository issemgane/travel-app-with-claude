package com.wanderlust.api.itinerary;

import com.wanderlust.api.common.BaseEntity;
import com.wanderlust.api.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "itineraries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Itinerary extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "country_codes")
    private String countryCodes;

    @Column(name = "duration_days", nullable = false)
    private Integer durationDays;

    @Column(name = "estimated_budget_usd", precision = 10, scale = 2)
    private BigDecimal estimatedBudgetUsd;

    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    @Column(name = "is_published", nullable = false)
    @Builder.Default
    private Boolean isPublished = false;

    @Column(name = "cloned_from")
    private UUID clonedFrom;

    @OneToMany(mappedBy = "itinerary", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dayNumber ASC")
    @Builder.Default
    private List<ItineraryDay> days = new ArrayList<>();
}
