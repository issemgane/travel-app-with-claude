package com.wanderlust.api.user;

import com.wanderlust.api.common.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;

    @Transactional(readOnly = true)
    public UserDto getById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("User", id));
        return UserDto.from(user);
    }

    @Transactional(readOnly = true)
    public UserDto getByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> ApiException.notFound("User", username));
        return UserDto.from(user);
    }

    @Transactional
    public UserDto updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("User", userId));

        if (request.getDisplayName() != null) user.setDisplayName(request.getDisplayName());
        if (request.getBio() != null) user.setBio(request.getBio());
        if (request.getAvatarUrl() != null) user.setAvatarUrl(request.getAvatarUrl());
        if (request.getTravelStyle() != null) user.setTravelStyle(request.getTravelStyle());

        return UserDto.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public UserStatsDto getStats(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw ApiException.notFound("User", userId);
        }
        User user = userRepository.findById(userId).orElseThrow();

        return UserStatsDto.builder()
                .followersCount(followRepository.countByFollowingId(userId))
                .followingCount(followRepository.countByFollowerId(userId))
                .countriesVisited(user.getCountriesVisitedCount())
                .build();
    }
}
