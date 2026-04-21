package com.wanderlust.api.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private UUID userId;
    private UserDto userDto;
    private UserStatsDto statsDto;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        userDto = UserDto.builder()
                .id(userId)
                .username("wanderer")
                .displayName("World Wanderer")
                .bio("Love to travel")
                .avatarUrl("https://example.com/avatar.jpg")
                .travelStyle(TravelStyle.BACKPACKER)
                .countriesVisitedCount(12)
                .createdAt(Instant.now())
                .build();
        statsDto = UserStatsDto.builder()
                .postsCount(5)
                .followersCount(42)
                .followingCount(17)
                .countriesVisited(12)
                .itinerariesCount(3)
                .build();
    }

    @Test
    @DisplayName("getUser returns OK with UserDto")
    void getUserReturnsOkWithUserDto() {
        when(userService.getById(userId)).thenReturn(userDto);

        ResponseEntity<UserDto> response = userController.getUser(userId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(userId);
        assertThat(response.getBody().getUsername()).isEqualTo("wanderer");
        verify(userService).getById(userId);
    }

    @Test
    @DisplayName("getUserByUsername returns OK with UserDto")
    void getUserByUsernameReturnsOkWithUserDto() {
        when(userService.getByUsername("wanderer")).thenReturn(userDto);

        ResponseEntity<UserDto> response = userController.getUserByUsername("wanderer");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUsername()).isEqualTo("wanderer");
        verify(userService).getByUsername("wanderer");
    }

    @Test
    @DisplayName("updateProfile returns OK with updated UserDto")
    void updateProfileReturnsOkWithUpdatedUserDto() {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setDisplayName("Updated Name");
        request.setTravelStyle(TravelStyle.LUXURY);

        UserDto updatedDto = UserDto.builder()
                .id(userId)
                .username("wanderer")
                .displayName("Updated Name")
                .bio("Love to travel")
                .avatarUrl("https://example.com/avatar.jpg")
                .travelStyle(TravelStyle.LUXURY)
                .countriesVisitedCount(12)
                .createdAt(userDto.getCreatedAt())
                .build();
        when(userService.updateProfile(userId, request)).thenReturn(updatedDto);

        ResponseEntity<UserDto> response = userController.updateProfile(userId, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDisplayName()).isEqualTo("Updated Name");
        assertThat(response.getBody().getTravelStyle()).isEqualTo(TravelStyle.LUXURY);
        verify(userService).updateProfile(userId, request);
    }

    @Test
    @DisplayName("getStats returns OK with UserStatsDto")
    void getStatsReturnsOkWithUserStatsDto() {
        when(userService.getStats(userId)).thenReturn(statsDto);

        ResponseEntity<UserStatsDto> response = userController.getStats(userId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getFollowersCount()).isEqualTo(42);
        assertThat(response.getBody().getFollowingCount()).isEqualTo(17);
        assertThat(response.getBody().getCountriesVisited()).isEqualTo(12);
        verify(userService).getStats(userId);
    }
}
