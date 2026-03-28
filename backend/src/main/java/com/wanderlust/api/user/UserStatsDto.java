package com.wanderlust.api.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsDto {
    private long postsCount;
    private long followersCount;
    private long followingCount;
    private int countriesVisited;
    private long itinerariesCount;
}
