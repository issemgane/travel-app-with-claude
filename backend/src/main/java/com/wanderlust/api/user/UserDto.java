package com.wanderlust.api.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private UUID id;
    private String username;
    private String displayName;
    private String bio;
    private String avatarUrl;
    private TravelStyle travelStyle;
    private Integer countriesVisitedCount;
    private Instant createdAt;

    public static UserDto from(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .bio(user.getBio())
                .avatarUrl(user.getAvatarUrl())
                .travelStyle(user.getTravelStyle())
                .countriesVisitedCount(user.getCountriesVisitedCount())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
