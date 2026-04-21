package com.wanderlust.api.user;

import com.wanderlust.api.common.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FollowRepository followRepository;

    @InjectMocks
    private UserService userService;

    private UUID userId;
    private User user;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = User.builder()
                .username("wanderer")
                .displayName("World Wanderer")
                .email("wanderer@example.com")
                .passwordHash("hashed")
                .bio("Love to travel")
                .avatarUrl("https://example.com/avatar.jpg")
                .travelStyle(TravelStyle.BACKPACKER)
                .countriesVisitedCount(12)
                .build();
        user.setId(userId);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
    }

    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("returns UserDto when user is found")
        void returnsUserDtoWhenFound() {
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            UserDto result = userService.getById(userId);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(userId);
            assertThat(result.getUsername()).isEqualTo("wanderer");
            assertThat(result.getDisplayName()).isEqualTo("World Wanderer");
            assertThat(result.getBio()).isEqualTo("Love to travel");
            assertThat(result.getAvatarUrl()).isEqualTo("https://example.com/avatar.jpg");
            assertThat(result.getTravelStyle()).isEqualTo(TravelStyle.BACKPACKER);
            assertThat(result.getCountriesVisitedCount()).isEqualTo(12);
            verify(userRepository).findById(userId);
        }

        @Test
        @DisplayName("throws ApiException NOT_FOUND when user does not exist")
        void throwsWhenNotFound() {
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getById(userId))
                    .isInstanceOf(ApiException.class)
                    .satisfies(ex -> {
                        ApiException apiEx = (ApiException) ex;
                        assertThat(apiEx.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    });
            verify(userRepository).findById(userId);
        }
    }

    @Nested
    @DisplayName("getByUsername")
    class GetByUsername {

        @Test
        @DisplayName("returns UserDto when user is found")
        void returnsUserDtoWhenFound() {
            when(userRepository.findByUsername("wanderer")).thenReturn(Optional.of(user));

            UserDto result = userService.getByUsername("wanderer");

            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo("wanderer");
            assertThat(result.getId()).isEqualTo(userId);
            verify(userRepository).findByUsername("wanderer");
        }

        @Test
        @DisplayName("throws ApiException NOT_FOUND when user does not exist")
        void throwsWhenNotFound() {
            when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getByUsername("nonexistent"))
                    .isInstanceOf(ApiException.class)
                    .satisfies(ex -> {
                        ApiException apiEx = (ApiException) ex;
                        assertThat(apiEx.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    });
            verify(userRepository).findByUsername("nonexistent");
        }
    }

    @Nested
    @DisplayName("updateProfile")
    class UpdateProfile {

        @Test
        @DisplayName("updates only non-null fields and saves")
        void updatesOnlyNonNullFields() {
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            UpdateProfileRequest request = new UpdateProfileRequest();
            request.setDisplayName("New Name");
            request.setBio(null);
            request.setAvatarUrl(null);
            request.setTravelStyle(TravelStyle.LUXURY);

            UserDto result = userService.updateProfile(userId, request);

            assertThat(result.getDisplayName()).isEqualTo("New Name");
            assertThat(result.getBio()).isEqualTo("Love to travel"); // unchanged
            assertThat(result.getAvatarUrl()).isEqualTo("https://example.com/avatar.jpg"); // unchanged
            assertThat(result.getTravelStyle()).isEqualTo(TravelStyle.LUXURY);
            verify(userRepository).findById(userId);
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("updates all fields when all are non-null")
        void updatesAllFieldsWhenAllNonNull() {
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            UpdateProfileRequest request = new UpdateProfileRequest();
            request.setDisplayName("Updated Name");
            request.setBio("Updated bio");
            request.setAvatarUrl("https://example.com/new-avatar.jpg");
            request.setTravelStyle(TravelStyle.SOLO);

            UserDto result = userService.updateProfile(userId, request);

            assertThat(result.getDisplayName()).isEqualTo("Updated Name");
            assertThat(result.getBio()).isEqualTo("Updated bio");
            assertThat(result.getAvatarUrl()).isEqualTo("https://example.com/new-avatar.jpg");
            assertThat(result.getTravelStyle()).isEqualTo(TravelStyle.SOLO);
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("throws ApiException NOT_FOUND when user does not exist")
        void throwsWhenNotFound() {
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            UpdateProfileRequest request = new UpdateProfileRequest();
            request.setDisplayName("New Name");

            assertThatThrownBy(() -> userService.updateProfile(userId, request))
                    .isInstanceOf(ApiException.class)
                    .satisfies(ex -> {
                        ApiException apiEx = (ApiException) ex;
                        assertThat(apiEx.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    });
            verify(userRepository).findById(userId);
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getStats")
    class GetStats {

        @Test
        @DisplayName("returns correct counts")
        void returnsCorrectCounts() {
            when(userRepository.existsById(userId)).thenReturn(true);
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(followRepository.countByFollowingId(userId)).thenReturn(42L);
            when(followRepository.countByFollowerId(userId)).thenReturn(17L);

            UserStatsDto result = userService.getStats(userId);

            assertThat(result).isNotNull();
            assertThat(result.getFollowersCount()).isEqualTo(42L);
            assertThat(result.getFollowingCount()).isEqualTo(17L);
            assertThat(result.getCountriesVisited()).isEqualTo(12);
            verify(followRepository).countByFollowingId(userId);
            verify(followRepository).countByFollowerId(userId);
        }

        @Test
        @DisplayName("throws ApiException NOT_FOUND when user does not exist")
        void throwsWhenNotFound() {
            when(userRepository.existsById(userId)).thenReturn(false);

            assertThatThrownBy(() -> userService.getStats(userId))
                    .isInstanceOf(ApiException.class)
                    .satisfies(ex -> {
                        ApiException apiEx = (ApiException) ex;
                        assertThat(apiEx.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    });
            verify(userRepository).existsById(userId);
            verify(followRepository, never()).countByFollowingId(any());
            verify(followRepository, never()).countByFollowerId(any());
        }
    }
}
