package com.wanderlust.api.user;

import com.wanderlust.api.common.ApiException;
import com.wanderlust.api.common.PagedResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FollowControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private FollowRepository followRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FollowController followController;

    private UUID currentUserId;
    private UUID targetUserId;

    @BeforeEach
    void setUp() {
        currentUserId = UUID.randomUUID();
        targetUserId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("checkFollow")
    class CheckFollow {

        @Test
        @DisplayName("returns true when current user follows target")
        void returnsTrueWhenFollowing() {
            FollowId followId = new FollowId(currentUserId, targetUserId);
            when(followRepository.existsById(followId)).thenReturn(true);

            ResponseEntity<FollowStatusDto> response = followController.checkFollow(currentUserId, targetUserId);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().isFollowing()).isTrue();
            verify(followRepository).existsById(followId);
        }

        @Test
        @DisplayName("returns false when current user does not follow target")
        void returnsFalseWhenNotFollowing() {
            FollowId followId = new FollowId(currentUserId, targetUserId);
            when(followRepository.existsById(followId)).thenReturn(false);

            ResponseEntity<FollowStatusDto> response = followController.checkFollow(currentUserId, targetUserId);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().isFollowing()).isFalse();
            verify(followRepository).existsById(followId);
        }
    }

    @Nested
    @DisplayName("follow")
    class FollowUser {

        @Test
        @DisplayName("creates follow and returns 201 CREATED")
        void createsFollowSuccessfully() {
            FollowId followId = new FollowId(currentUserId, targetUserId);
            when(userRepository.existsById(targetUserId)).thenReturn(true);
            when(followRepository.existsById(followId)).thenReturn(false);
            when(followRepository.save(any(Follow.class))).thenReturn(Follow.builder().id(followId).build());

            ResponseEntity<Void> response = followController.follow(currentUserId, targetUserId);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            verify(followRepository).save(any(Follow.class));
        }

        @Test
        @DisplayName("throws ApiException BAD_REQUEST when following self")
        void throwsWhenFollowingSelf() {
            assertThatThrownBy(() -> followController.follow(currentUserId, currentUserId))
                    .isInstanceOf(ApiException.class)
                    .satisfies(ex -> {
                        ApiException apiEx = (ApiException) ex;
                        assertThat(apiEx.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                        assertThat(apiEx.getMessage()).contains("Cannot follow yourself");
                    });
            verify(followRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws ApiException NOT_FOUND when target user does not exist")
        void throwsWhenUserNotFound() {
            when(userRepository.existsById(targetUserId)).thenReturn(false);

            assertThatThrownBy(() -> followController.follow(currentUserId, targetUserId))
                    .isInstanceOf(ApiException.class)
                    .satisfies(ex -> {
                        ApiException apiEx = (ApiException) ex;
                        assertThat(apiEx.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    });
            verify(followRepository, never()).save(any());
        }

        @Test
        @DisplayName("returns OK when already following")
        void returnsOkWhenAlreadyFollowing() {
            FollowId followId = new FollowId(currentUserId, targetUserId);
            when(userRepository.existsById(targetUserId)).thenReturn(true);
            when(followRepository.existsById(followId)).thenReturn(true);

            ResponseEntity<Void> response = followController.follow(currentUserId, targetUserId);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(followRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("unfollow")
    class Unfollow {

        @Test
        @DisplayName("deletes follow and returns 204 NO_CONTENT")
        void deletesFollowSuccessfully() {
            FollowId followId = new FollowId(currentUserId, targetUserId);

            ResponseEntity<Void> response = followController.unfollow(currentUserId, targetUserId);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            verify(followRepository).deleteById(followId);
        }
    }

    @Nested
    @DisplayName("getFollowers")
    class GetFollowers {

        @Test
        @DisplayName("returns PagedResponse of followers")
        void returnsPagedResponseOfFollowers() {
            Pageable pageable = PageRequest.of(0, 20);
            UUID followerId = UUID.randomUUID();
            UserDto followerDto = UserDto.builder()
                    .id(followerId)
                    .username("follower1")
                    .displayName("Follower One")
                    .createdAt(Instant.now())
                    .build();

            Page<UUID> followerIdsPage = new PageImpl<>(List.of(followerId), pageable, 1);
            when(followRepository.findFollowerIds(targetUserId, pageable)).thenReturn(followerIdsPage);
            when(userService.getById(followerId)).thenReturn(followerDto);

            ResponseEntity<PagedResponse<UserDto>> response = followController.getFollowers(targetUserId, pageable);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).hasSize(1);
            assertThat(response.getBody().getContent().get(0).getUsername()).isEqualTo("follower1");
            assertThat(response.getBody().getTotalElements()).isEqualTo(1);
            assertThat(response.getBody().getPage()).isZero();
            verify(followRepository).findFollowerIds(targetUserId, pageable);
            verify(userService).getById(followerId);
        }
    }

    @Nested
    @DisplayName("getFollowing")
    class GetFollowing {

        @Test
        @DisplayName("returns PagedResponse of following users")
        void returnsPagedResponseOfFollowing() {
            Pageable pageable = PageRequest.of(0, 20);
            UUID followingId = UUID.randomUUID();
            UserDto followingDto = UserDto.builder()
                    .id(followingId)
                    .username("following1")
                    .displayName("Following One")
                    .createdAt(Instant.now())
                    .build();

            Page<UUID> followingIdsPage = new PageImpl<>(List.of(followingId), pageable, 1);
            when(followRepository.findFollowingIds(targetUserId, pageable)).thenReturn(followingIdsPage);
            when(userService.getById(followingId)).thenReturn(followingDto);

            ResponseEntity<PagedResponse<UserDto>> response = followController.getFollowing(targetUserId, pageable);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).hasSize(1);
            assertThat(response.getBody().getContent().get(0).getUsername()).isEqualTo("following1");
            assertThat(response.getBody().getTotalElements()).isEqualTo(1);
            assertThat(response.getBody().getPage()).isZero();
            verify(followRepository).findFollowingIds(targetUserId, pageable);
            verify(userService).getById(followingId);
        }
    }
}
