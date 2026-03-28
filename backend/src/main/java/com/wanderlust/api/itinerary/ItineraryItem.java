package com.wanderlust.api.itinerary;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "itinerary_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItineraryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "day_id", nullable = false)
    private ItineraryDay day;

    @Column(name = "post_id")
    private UUID postId;

    @Column(name = "custom_title", length = 200)
    private String customTitle;

    @Column(name = "custom_note", columnDefinition = "TEXT")
    private String customNote;

    @Column(name = "transport_to_next", length = 100)
    private String transportToNext;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;
}
