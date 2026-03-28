package com.wanderlust.api.itinerary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItineraryDto {
    private UUID id;
    private UUID userId;
    private String username;
    private String title;
    private String description;
    private String[] countryCodes;
    private Integer durationDays;
    private BigDecimal estimatedBudgetUsd;
    private String coverImageUrl;
    private Boolean isPublished;
    private UUID clonedFrom;
    private List<DayDto> days;
    private Instant createdAt;
    private Instant updatedAt;

    public static ItineraryDto from(Itinerary itinerary) {
        return ItineraryDto.builder()
                .id(itinerary.getId())
                .userId(itinerary.getUser().getId())
                .username(itinerary.getUser().getUsername())
                .title(itinerary.getTitle())
                .description(itinerary.getDescription())
                .countryCodes(itinerary.getCountryCodes())
                .durationDays(itinerary.getDurationDays())
                .estimatedBudgetUsd(itinerary.getEstimatedBudgetUsd())
                .coverImageUrl(itinerary.getCoverImageUrl())
                .isPublished(itinerary.getIsPublished())
                .clonedFrom(itinerary.getClonedFrom())
                .days(itinerary.getDays().stream().map(DayDto::from).toList())
                .createdAt(itinerary.getCreatedAt())
                .updatedAt(itinerary.getUpdatedAt())
                .build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DayDto {
        private UUID id;
        private Integer dayNumber;
        private String title;
        private String notes;
        private List<ItemDto> items;

        public static DayDto from(ItineraryDay day) {
            return DayDto.builder()
                    .id(day.getId())
                    .dayNumber(day.getDayNumber())
                    .title(day.getTitle())
                    .notes(day.getNotes())
                    .items(day.getItems().stream().map(ItemDto::from).toList())
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemDto {
        private UUID id;
        private UUID postId;
        private String customTitle;
        private String customNote;
        private String transportToNext;
        private Integer displayOrder;

        public static ItemDto from(ItineraryItem item) {
            return ItemDto.builder()
                    .id(item.getId())
                    .postId(item.getPostId())
                    .customTitle(item.getCustomTitle())
                    .customNote(item.getCustomNote())
                    .transportToNext(item.getTransportToNext())
                    .displayOrder(item.getDisplayOrder())
                    .build();
        }
    }
}
