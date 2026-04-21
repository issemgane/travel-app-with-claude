package com.wanderlust.api.post;

import com.wanderlust.api.common.PagedResponse;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TravelPostControllerTest {

    @Mock
    private TravelPostService postService;

    @InjectMocks
    private TravelPostController controller;

    private PostDto createPostDto() {
        return PostDto.builder()
                .id(UUID.randomUUID())
                .user(PostDto.UserSummary.builder()
                        .id(UUID.randomUUID())
                        .username("traveler42")
                        .displayName("Jane Traveler")
                        .avatarUrl("https://example.com/avatar.jpg")
                        .build())
                .content("Amazing place!")
                .category(PostCategory.SPOT)
                .latitude(35.6762)
                .longitude(139.6503)
                .placeName("Shibuya")
                .countryCode("JP")
                .tags(new String[]{"tokyo"})
                .likesCount(5)
                .commentsCount(2)
                .media(List.of())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private Page<PostDto> createPostDtoPage(Pageable pageable) {
        return new PageImpl<>(List.of(createPostDto()), pageable, 1);
    }

    @Test
    void create_returns201WithPostDto() {
        UUID userId = UUID.randomUUID();
        CreatePostRequest request = new CreatePostRequest();
        PostDto dto = createPostDto();

        when(postService.create(eq(userId), any(CreatePostRequest.class))).thenReturn(dto);

        ResponseEntity<PostDto> response = controller.create(userId, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(dto);
    }

    @Test
    void getById_returns200WithPostDto() {
        UUID postId = UUID.randomUUID();
        PostDto dto = createPostDto();

        when(postService.getById(postId)).thenReturn(dto);

        ResponseEntity<PostDto> response = controller.getById(postId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dto);
    }

    @Test
    void delete_returns204() {
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();

        ResponseEntity<Void> response = controller.delete(userId, postId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(postService).delete(postId, userId);
    }

    @Test
    void getFeed_returns200WithPagedResponse() {
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        Page<PostDto> page = createPostDtoPage(pageable);

        when(postService.getFeed(userId, pageable)).thenReturn(page);

        ResponseEntity<PagedResponse<PostDto>> response = controller.getFeed(userId, pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
    }

    @Test
    void getNearby_returns200WithPagedResponse() {
        double lat = 35.6762;
        double lng = 139.6503;
        double radius = 10000;
        Pageable pageable = PageRequest.of(0, 10);
        Page<PostDto> page = createPostDtoPage(pageable);

        when(postService.getNearby(lat, lng, radius, pageable)).thenReturn(page);

        ResponseEntity<PagedResponse<PostDto>> response = controller.getNearby(lat, lng, radius, pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
    }

    @Test
    void getByDestination_returns200WithPagedResponse() {
        String countryCode = "JP";
        Pageable pageable = PageRequest.of(0, 10);
        Page<PostDto> page = createPostDtoPage(pageable);

        when(postService.getByDestination(countryCode, pageable)).thenReturn(page);

        ResponseEntity<PagedResponse<PostDto>> response = controller.getByDestination(countryCode, pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
    }

    @Test
    void search_returns200WithPagedResponse() {
        String query = "tokyo";
        Pageable pageable = PageRequest.of(0, 10);
        Page<PostDto> page = createPostDtoPage(pageable);

        when(postService.search(query, pageable)).thenReturn(page);

        ResponseEntity<PagedResponse<PostDto>> response = controller.search(query, pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
    }

    @Test
    void getByUser_returns200WithPagedResponse() {
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        Page<PostDto> page = createPostDtoPage(pageable);

        when(postService.getByUser(userId, pageable)).thenReturn(page);

        ResponseEntity<PagedResponse<PostDto>> response = controller.getByUser(userId, pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
    }
}
