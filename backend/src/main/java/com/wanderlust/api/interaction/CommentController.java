package com.wanderlust.api.interaction;

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
@RequestMapping("/api/posts/{postId}/comments")
@RequiredArgsConstructor
@Tag(name = "Comments")
public class CommentController {

    private final InteractionService interactionService;
    private final UserService userService;

    @GetMapping
    @Operation(summary = "Get comments for a post")
    public ResponseEntity<PagedResponse<CommentDto>> getComments(
            @PathVariable UUID postId,
            Pageable pageable) {
        return ResponseEntity.ok(PagedResponse.from(interactionService.getComments(postId, pageable)));
    }

    @PostMapping
    @Operation(summary = "Add a comment to a post")
    public ResponseEntity<CommentDto> addComment(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID postId,
            @Valid @RequestBody CreateCommentRequest request) {
        UUID userId = userService.resolveUserId(jwt);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(interactionService.addComment(userId, postId, request));
    }

    @GetMapping("/questions")
    @Operation(summary = "Get Q&A for a post")
    public ResponseEntity<PagedResponse<CommentDto>> getQuestions(
            @PathVariable UUID postId,
            Pageable pageable) {
        return ResponseEntity.ok(PagedResponse.from(interactionService.getQuestions(postId, pageable)));
    }
}
