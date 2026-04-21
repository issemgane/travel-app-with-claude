package com.wanderlust.api.interaction;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LikeControllerTest {

    @Mock
    private InteractionService interactionService;

    @InjectMocks
    private LikeController controller;

    @Test
    void toggleLike_returns200WithLikedTrue() {
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();

        when(interactionService.toggleLike(userId, postId)).thenReturn(true);

        ResponseEntity<Map<String, Boolean>> response = controller.toggleLike(userId, postId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("liked")).isTrue();
    }

    @Test
    void toggleLike_returns200WithLikedFalse() {
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();

        when(interactionService.toggleLike(userId, postId)).thenReturn(false);

        ResponseEntity<Map<String, Boolean>> response = controller.toggleLike(userId, postId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("liked")).isFalse();
    }

    @Test
    void unlike_returns204() {
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();

        ResponseEntity<Void> response = controller.unlike(userId, postId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(interactionService).toggleLike(userId, postId);
    }
}
