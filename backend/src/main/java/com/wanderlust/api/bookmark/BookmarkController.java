package com.wanderlust.api.bookmark;

import com.wanderlust.api.common.PagedResponse;
import com.wanderlust.api.post.PostDto;
import com.wanderlust.api.post.TravelPostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/bookmarks")
@RequiredArgsConstructor
@Tag(name = "Bookmarks")
public class BookmarkController {

    private final BookmarkRepository bookmarkRepository;
    private final TravelPostService postService;

    @PostMapping("/{postId}")
    @Operation(summary = "Bookmark a post")
    public ResponseEntity<Void> bookmark(@AuthenticationPrincipal UUID userId, @PathVariable UUID postId) {
        BookmarkId id = new BookmarkId(userId, postId);
        if (!bookmarkRepository.existsById(id)) {
            bookmarkRepository.save(Bookmark.builder().id(id).build());
        }
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{postId}")
    @Operation(summary = "Remove bookmark")
    public ResponseEntity<Void> removeBookmark(@AuthenticationPrincipal UUID userId, @PathVariable UUID postId) {
        bookmarkRepository.deleteById(new BookmarkId(userId, postId));
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Get bookmarked posts")
    public ResponseEntity<PagedResponse<PostDto>> getBookmarks(
            @AuthenticationPrincipal UUID userId, Pageable pageable) {
        var postIds = bookmarkRepository.findPostIdsByUserId(userId, pageable);
        var posts = postIds.map(postService::getById);
        return ResponseEntity.ok(PagedResponse.from(posts));
    }

    @GetMapping("/{postId}/status")
    @Operation(summary = "Check if current user has bookmarked a post")
    public ResponseEntity<BookmarkStatusDto> checkBookmark(
            @AuthenticationPrincipal UUID userId, @PathVariable UUID postId) {
        boolean bookmarked = bookmarkRepository.existsById(new BookmarkId(userId, postId));
        return ResponseEntity.ok(new BookmarkStatusDto(bookmarked));
    }
}
