package com.wanderlust.api.post;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostDto {
    private UUID id;
    private UserSummary user;
    private String content;
    private PostCategory category;
    private Short costLevel;
    private String bestSeason;
    private String durationSuggested;
    private Short accessibilityRating;
    private Double latitude;
    private Double longitude;
    private String placeName;
    private String countryCode;
    private String[] tags;
    private Integer likesCount;
    private Integer commentsCount;
    private List<MediaDto> media;
    private Instant createdAt;
    private Instant updatedAt;

    public static PostDto from(TravelPost post) {
        return PostDto.builder()
                .id(post.getId())
                .user(UserSummary.builder()
                        .id(post.getUser().getId())
                        .username(post.getUser().getUsername())
                        .displayName(post.getUser().getDisplayName())
                        .avatarUrl(post.getUser().getAvatarUrl())
                        .build())
                .content(post.getContent())
                .category(post.getCategory())
                .costLevel(post.getCostLevel())
                .bestSeason(post.getBestSeason())
                .durationSuggested(post.getDurationSuggested())
                .accessibilityRating(post.getAccessibilityRating())
                .latitude(post.getLocation() != null ? post.getLocation().getY() : null)
                .longitude(post.getLocation() != null ? post.getLocation().getX() : null)
                .placeName(post.getPlaceName())
                .countryCode(post.getCountryCode())
                .tags(post.getTags())
                .likesCount(post.getLikesCount())
                .commentsCount(post.getCommentsCount())
                .media(post.getMedia().stream().map(m -> MediaDto.builder()
                        .id(m.getId())
                        .mediaUrl(m.getMediaUrl())
                        .mediaType(m.getMediaType())
                        .displayOrder(m.getDisplayOrder())
                        .width(m.getWidth())
                        .height(m.getHeight())
                        .build()).toList())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserSummary {
        private UUID id;
        private String username;
        private String displayName;
        private String avatarUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MediaDto {
        private UUID id;
        private String mediaUrl;
        private MediaType mediaType;
        private Integer displayOrder;
        private Integer width;
        private Integer height;
    }
}
