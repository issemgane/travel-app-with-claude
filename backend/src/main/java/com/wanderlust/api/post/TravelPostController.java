package com.wanderlust.api.post;

import com.wanderlust.api.common.PagedResponse;
import com.wanderlust.api.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Tag(name = "Travel Posts")
public class TravelPostController {

    private final TravelPostService postService;
    private final UserService userService;

    @PostMapping
    @Operation(summary = "Create a new Travel Card")
    public ResponseEntity<PostDto> create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreatePostRequest request) {
        UUID userId = userService.resolveUserId(jwt);
        return ResponseEntity.status(HttpStatus.CREATED).body(postService.create(userId, request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get post by ID")
    public ResponseEntity<PostDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(postService.getById(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete own post")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id) {
        UUID userId = userService.resolveUserId(jwt);
        postService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/feed")
    @Operation(summary = "Get personalized feed")
    public ResponseEntity<PagedResponse<PostDto>> getFeed(
            @AuthenticationPrincipal Jwt jwt,
            Pageable pageable) {
        UUID userId = userService.resolveUserId(jwt);
        return ResponseEntity.ok(PagedResponse.from(postService.getFeed(userId, pageable)));
    }

    @GetMapping("/near")
    @Operation(summary = "Get posts near a location")
    public ResponseEntity<PagedResponse<PostDto>> getNearby(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "10000") double radius,
            Pageable pageable) {
        return ResponseEntity.ok(PagedResponse.from(postService.getNearby(lat, lng, radius, pageable)));
    }

    @GetMapping("/destination/{countryCode}")
    @Operation(summary = "Get posts by country code")
    public ResponseEntity<PagedResponse<PostDto>> getByDestination(
            @PathVariable String countryCode,
            Pageable pageable) {
        return ResponseEntity.ok(PagedResponse.from(postService.getByDestination(countryCode, pageable)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search posts by content")
    public ResponseEntity<PagedResponse<PostDto>> search(
            @RequestParam String q,
            Pageable pageable) {
        return ResponseEntity.ok(PagedResponse.from(postService.search(q, pageable)));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get posts by user")
    public ResponseEntity<PagedResponse<PostDto>> getByUser(
            @PathVariable UUID userId,
            Pageable pageable) {
        return ResponseEntity.ok(PagedResponse.from(postService.getByUser(userId, pageable)));
    }
}
