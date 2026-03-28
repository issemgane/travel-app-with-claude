package com.wanderlust.api.user;

import com.wanderlust.api.common.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;

    @Transactional
    public UserDto getOrCreateFromJwt(Jwt jwt) {
        String keycloakId = jwt.getSubject();
        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseGet(() -> createFromJwt(jwt));
        return UserDto.from(user);
    }

    private User createFromJwt(Jwt jwt) {
        String preferredUsername = jwt.getClaimAsString("preferred_username");
        String name = jwt.getClaimAsString("name");
        if (name == null) name = preferredUsername;

        // Ensure unique username
        String username = preferredUsername;
        int suffix = 1;
        while (userRepository.existsByUsername(username)) {
            username = preferredUsername + suffix++;
        }

        User user = User.builder()
                .keycloakId(jwt.getSubject())
                .username(username)
                .displayName(name)
                .build();
        return userRepository.save(user);
    }

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

    public UUID resolveUserId(Jwt jwt) {
        String keycloakId = jwt.getSubject();
        return userRepository.findByKeycloakId(keycloakId)
                .map(User::getId)
                .orElseThrow(() -> ApiException.notFound("User", keycloakId));
    }
}
