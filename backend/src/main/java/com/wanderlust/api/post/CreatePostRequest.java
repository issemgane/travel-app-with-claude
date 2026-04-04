package com.wanderlust.api.post;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class CreatePostRequest {

    @NotBlank(message = "Content is required")
    @Size(max = 5000)
    private String content;

    private PostCategory category;

    @NotNull(message = "Latitude is required")
    @Min(-90) @Max(90)
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @Min(-180) @Max(180)
    private Double longitude;

    @NotBlank(message = "Place name is required")
    @Size(max = 255)
    private String placeName;

    @NotBlank(message = "Country code is required")
    @Size(max = 3)
    private String countryCode;

    @Min(1) @Max(5)
    private Short costLevel;

    @Size(max = 20)
    private String bestSeason;

    @Size(max = 50)
    private String durationSuggested;

    @Min(1) @Max(5)
    private Short accessibilityRating;

    private String[] tags;

    @NotEmpty(message = "At least one media item is required")
    @Size(max = 10)
    private List<MediaItem> mediaItems;

    @Data
    public static class MediaItem {
        @NotBlank
        private String mediaUrl;
        private MediaType mediaType = MediaType.IMAGE;
        private Integer width;
        private Integer height;
    }
}
