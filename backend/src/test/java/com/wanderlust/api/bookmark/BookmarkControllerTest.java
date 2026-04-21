package com.wanderlust.api.bookmark;

import com.wanderlust.api.common.PagedResponse;
import com.wanderlust.api.post.PostDto;
import com.wanderlust.api.post.TravelPostService;
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

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookmarkControllerTest {

    @Mock
    private BookmarkRepository bookmarkRepository;

    @Mock
    private TravelPostService postService;

    @InjectMocks
    private BookmarkController bookmarkController;

    private UUID userId;
    private UUID postId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        postId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("bookmark")
    class BookmarkPost {

        @Test
        @DisplayName("saves bookmark when not already bookmarked and returns 201")
        void savesWhenNotAlreadyBookmarked() {
            BookmarkId bookmarkId = new BookmarkId(userId, postId);
            when(bookmarkRepository.existsById(bookmarkId)).thenReturn(false);
            when(bookmarkRepository.save(any(Bookmark.class))).thenAnswer(inv -> inv.getArgument(0));

            ResponseEntity<Void> response = bookmarkController.bookmark(userId, postId);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            verify(bookmarkRepository).existsById(bookmarkId);
            verify(bookmarkRepository).save(any(Bookmark.class));
        }

        @Test
        @DisplayName("does not duplicate when already bookmarked and returns 201")
        void doesNotDuplicateWhenAlreadyBookmarked() {
            BookmarkId bookmarkId = new BookmarkId(userId, postId);
            when(bookmarkRepository.existsById(bookmarkId)).thenReturn(true);

            ResponseEntity<Void> response = bookmarkController.bookmark(userId, postId);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            verify(bookmarkRepository).existsById(bookmarkId);
            verify(bookmarkRepository, never()).save(any(Bookmark.class));
        }
    }

    @Nested
    @DisplayName("removeBookmark")
    class RemoveBookmark {

        @Test
        @DisplayName("deletes bookmark and returns 204")
        void deletesAndReturns204() {
            BookmarkId bookmarkId = new BookmarkId(userId, postId);

            ResponseEntity<Void> response = bookmarkController.removeBookmark(userId, postId);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            verify(bookmarkRepository).deleteById(bookmarkId);
        }
    }

    @Nested
    @DisplayName("getBookmarks")
    class GetBookmarks {

        @Test
        @DisplayName("returns PagedResponse with mapped posts")
        void returnsPagedResponseWithPosts() {
            Pageable pageable = PageRequest.of(0, 10);
            UUID postId1 = UUID.randomUUID();
            UUID postId2 = UUID.randomUUID();

            Page<UUID> postIdPage = new PageImpl<>(List.of(postId1, postId2), pageable, 2);
            when(bookmarkRepository.findPostIdsByUserId(userId, pageable)).thenReturn(postIdPage);

            PostDto dto1 = PostDto.builder().id(postId1).build();
            PostDto dto2 = PostDto.builder().id(postId2).build();
            when(postService.getById(postId1)).thenReturn(dto1);
            when(postService.getById(postId2)).thenReturn(dto2);

            ResponseEntity<PagedResponse<PostDto>> response = bookmarkController.getBookmarks(userId, pageable);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).hasSize(2);
            assertThat(response.getBody().getContent().get(0).getId()).isEqualTo(postId1);
            assertThat(response.getBody().getContent().get(1).getId()).isEqualTo(postId2);
            assertThat(response.getBody().getTotalElements()).isEqualTo(2);
            verify(bookmarkRepository).findPostIdsByUserId(userId, pageable);
            verify(postService).getById(postId1);
            verify(postService).getById(postId2);
        }
    }

    @Nested
    @DisplayName("getBookmarkIds")
    class GetBookmarkIds {

        @Test
        @DisplayName("returns list of bookmarked post IDs")
        void returnsListOfPostIds() {
            UUID postId1 = UUID.randomUUID();
            UUID postId2 = UUID.randomUUID();
            UUID postId3 = UUID.randomUUID();

            when(bookmarkRepository.findAllPostIdsByUserId(userId))
                    .thenReturn(List.of(postId1, postId2, postId3));

            ResponseEntity<List<UUID>> response = bookmarkController.getBookmarkIds(userId);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).containsExactly(postId1, postId2, postId3);
            verify(bookmarkRepository).findAllPostIdsByUserId(userId);
        }
    }
}
