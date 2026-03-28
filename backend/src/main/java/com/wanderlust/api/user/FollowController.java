package com.wanderlust.api.user;

import com.wanderlust.api.common.ApiException;
import com.wanderlust.api.common.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Follows")
public class FollowController {

    private final UserService userService;
    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    @PostMapping("/{id}/follow")
    @Operation(summary = "Follow a user")
    public ResponseEntity<Void> follow(@AuthenticationPrincipal UUID currentUserId, @PathVariable UUID id) {
        if (currentUserId.equals(id)) throw ApiException.badRequest("Cannot follow yourself");
        if (!userRepository.existsById(id)) throw ApiException.notFound("User", id);

        FollowId followId = new FollowId(currentUserId, id);
        if (followRepository.existsById(followId)) return ResponseEntity.ok().build();

        followRepository.save(Follow.builder().id(followId).build());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{id}/follow")
    @Operation(summary = "Unfollow a user")
    public ResponseEntity<Void> unfollow(@AuthenticationPrincipal UUID currentUserId, @PathVariable UUID id) {
        followRepository.deleteById(new FollowId(currentUserId, id));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/followers")
    @Operation(summary = "Get user's followers")
    public ResponseEntity<PagedResponse<UserDto>> getFollowers(@PathVariable UUID id, Pageable pageable) {
        Page<UserDto> followers = followRepository.findFollowerIds(id, pageable).map(userService::getById);
        return ResponseEntity.ok(PagedResponse.from(followers));
    }

    @GetMapping("/{id}/following")
    @Operation(summary = "Get users that user follows")
    public ResponseEntity<PagedResponse<UserDto>> getFollowing(@PathVariable UUID id, Pageable pageable) {
        Page<UserDto> following = followRepository.findFollowingIds(id, pageable).map(userService::getById);
        return ResponseEntity.ok(PagedResponse.from(following));
    }
}
