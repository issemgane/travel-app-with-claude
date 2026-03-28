package com.wanderlust.api.interaction;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/posts/{postId}/like")
@RequiredArgsConstructor
@Tag(name = "Likes")
public class LikeController {

    private final InteractionService interactionService;

    @PostMapping
    @Operation(summary = "Toggle like on a post")
    public ResponseEntity<Map<String, Boolean>> toggleLike(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID postId) {
        boolean liked = interactionService.toggleLike(userId, postId);
        return ResponseEntity.ok(Map.of("liked", liked));
    }

    @DeleteMapping
    @Operation(summary = "Unlike a post")
    public ResponseEntity<Void> unlike(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID postId) {
        interactionService.toggleLike(userId, postId);
        return ResponseEntity.noContent().build();
    }
}
