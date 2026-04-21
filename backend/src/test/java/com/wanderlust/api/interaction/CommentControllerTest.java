package com.wanderlust.api.interaction;

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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentControllerTest {

    @Mock
    private InteractionService interactionService;

    @InjectMocks
    private CommentController controller;

    private CommentDto createCommentDto(UUID postId) {
        return CommentDto.builder()
                .id(UUID.randomUUID())
                .postId(postId)
                .userId(UUID.randomUUID())
                .username("traveler42")
                .displayName("Jane Traveler")
                .avatarUrl("https://example.com/avatar.jpg")
                .content("Great post!")
                .isQuestion(false)
                .isAnswer(false)
                .createdAt(Instant.now())
                .build();
    }

    @Test
    void getComments_returns200WithPagedResponse() {
        UUID postId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        CommentDto dto = createCommentDto(postId);
        Page<CommentDto> page = new PageImpl<>(List.of(dto), pageable, 1);

        when(interactionService.getComments(postId, pageable)).thenReturn(page);

        ResponseEntity<PagedResponse<CommentDto>> response = controller.getComments(postId, pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getContent().get(0).getContent()).isEqualTo("Great post!");
    }

    @Test
    void addComment_returns201WithCommentDto() {
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        CreateCommentRequest request = new CreateCommentRequest();
        request.setContent("Nice photo!");
        CommentDto dto = createCommentDto(postId);
        dto.setContent("Nice photo!");

        when(interactionService.addComment(eq(userId), eq(postId), any(CreateCommentRequest.class)))
                .thenReturn(dto);

        ResponseEntity<CommentDto> response = controller.addComment(userId, postId, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).isEqualTo("Nice photo!");
    }

    @Test
    void getQuestions_returns200WithPagedResponse() {
        UUID postId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        CommentDto dto = createCommentDto(postId);
        dto.setIsQuestion(true);
        dto.setContent("How to get there?");
        Page<CommentDto> page = new PageImpl<>(List.of(dto), pageable, 1);

        when(interactionService.getQuestions(postId, pageable)).thenReturn(page);

        ResponseEntity<PagedResponse<CommentDto>> response = controller.getQuestions(postId, pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getContent().get(0).getIsQuestion()).isTrue();
    }
}
