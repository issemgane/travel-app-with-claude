package com.wanderlust.api.discovery;

import com.wanderlust.api.post.PostDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DestinationSummaryDto {
    private String countryCode;
    private long postCount;
    private List<PostDto> topPosts;
    private Double averageCostLevel;
}
