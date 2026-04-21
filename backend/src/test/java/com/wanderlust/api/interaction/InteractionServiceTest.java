package com.wanderlust.api.interaction;

import com.wanderlust.api.common.ApiException;
import com.wanderlust.api.post.TravelPostRepository;
import com.wanderlust.api.user.User;
import com.wanderlust.api.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InteractionServiceTest {

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private TravelPostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private InteractionService interactionService;

    @Captor
    private ArgumentCaptor<Like> likeCaptor;

    @Captor
    private ArgumentCaptor<Comment> commentCaptor;

    private User createUser() {
        User user = User.builder()
                .username("traveler42")
                .displayName("Jane Traveler")
                .email("jane@example.com")
                .passwordHash("hashed")
                .avatarUrl("https://example.com/avatar.jpg")
                .build();
        user.setId(UUID.randomUUID());
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        return user;
    }

    private Comment createComment(User user, UUID postId) {
        Comment comment = Comment.builder()
                .postId(postId)
                .user(user)
                .content("Great post!")
                .isQuestion(false)
                .isAnswer(false)
                .build();
        comment.setId(UUID.randomUUID());
        comment.setCreatedAt(Instant.now());
        comment.setUpdatedAt(Instant.now());
        return comment;
    }

    // --- toggleLike tests ---

    @Test
    void toggleLike_returnsTrueWhenLiking() {
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();

        when(postRepository.existsById(postId)).thenReturn(true);
        when(likeRepository.existsByUserIdAndPostId(userId, postId)).thenReturn(false);
        when(likeRepository.save(any(Like.class))).thenAnswer(invocation -> invocation.getArgument(0));

        boolean result = interactionService.toggleLike(userId, postId);

        assertThat(result).isTrue();
        verify(likeRepository).save(likeCaptor.capture());
        Like savedLike = likeCaptor.getValue();
        assertThat(savedLike.getUserId()).isEqualTo(userId);
        assertThat(savedLike.getPostId()).isEqualTo(postId);
    }

    @Test
    void toggleLike_returnsFalseWhenUnliking() {
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();

        when(postRepository.existsById(postId)).thenReturn(true);
        when(likeRepository.existsByUserIdAndPostId(userId, postId)).thenReturn(true);

        boolean result = interactionService.toggleLike(userId, postId);

        assertThat(result).isFalse();
        verify(likeRepository).deleteByUserIdAndPostId(userId, postId);
        verify(likeRepository, never()).save(any());
    }

    @Test
    void toggleLike_throwsNotFoundWhenPostDoesNotExist() {
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();

        when(postRepository.existsById(postId)).thenReturn(false);

        assertThatThrownBy(() -> interactionService.toggleLike(userId, postId))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));

        verify(likeRepository, never()).save(any());
        verify(likeRepository, never()).deleteByUserIdAndPostId(any(), any());
    }

    // --- addComment tests ---

    @Test
    void addComment_createsCommentAndReturnsDto() {
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        User user = createUser();
        user.setId(userId);

        CreateCommentRequest request = new CreateCommentRequest();
        request.setContent("Great post!");
        request.setIsQuestion(false);
        request.setIsAnswer(false);

        when(postRepository.existsById(postId)).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            saved.setCreatedAt(Instant.now());
            saved.setUpdatedAt(Instant.now());
            return saved;
        });

        CommentDto result = interactionService.addComment(userId, postId, request);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo("Great post!");
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getPostId()).isEqualTo(postId);

        verify(commentRepository).save(commentCaptor.capture());
        Comment captured = commentCaptor.getValue();
        assertThat(captured.getContent()).isEqualTo("Great post!");
        assertThat(captured.getUser()).isEqualTo(user);
        assertThat(captured.getPostId()).isEqualTo(postId);
    }

    @Test
    void addComment_throwsWhenPostDoesNotExist() {
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        CreateCommentRequest request = new CreateCommentRequest();
        request.setContent("Great post!");

        when(postRepository.existsById(postId)).thenReturn(false);

        assertThatThrownBy(() -> interactionService.addComment(userId, postId, request))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));

        verify(commentRepository, never()).save(any());
    }

    @Test
    void addComment_throwsWhenUserDoesNotExist() {
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        CreateCommentRequest request = new CreateCommentRequest();
        request.setContent("Great post!");

        when(postRepository.existsById(postId)).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> interactionService.addComment(userId, postId, request))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));

        verify(commentRepository, never()).save(any());
    }

    // --- getComments tests ---

    @Test
    void getComments_returnsPage() {
        UUID postId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        User user = createUser();
        Comment comment = createComment(user, postId);
        Page<Comment> page = new PageImpl<>(List.of(comment), pageable, 1);

        when(commentRepository.findByPostIdOrderByCreatedAtDesc(postId, pageable)).thenReturn(page);

        Page<CommentDto> result = interactionService.getComments(postId, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getContent()).isEqualTo("Great post!");
        verify(commentRepository).findByPostIdOrderByCreatedAtDesc(postId, pageable);
    }

    // --- getQuestions tests ---

    @Test
    void getQuestions_returnsPage() {
        UUID postId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        User user = createUser();
        Comment question = createComment(user, postId);
        question.setIsQuestion(true);
        question.setContent("How to get there?");
        Page<Comment> page = new PageImpl<>(List.of(question), pageable, 1);

        when(commentRepository.findByPostIdAndIsQuestionTrueOrderByCreatedAtDesc(postId, pageable))
                .thenReturn(page);

        Page<CommentDto> result = interactionService.getQuestions(postId, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getIsQuestion()).isTrue();
        verify(commentRepository).findByPostIdAndIsQuestionTrueOrderByCreatedAtDesc(postId, pageable);
    }

    // --- hasLiked tests ---

    @Test
    void hasLiked_returnsTrueWhenLiked() {
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();

        when(likeRepository.existsByUserIdAndPostId(userId, postId)).thenReturn(true);

        assertThat(interactionService.hasLiked(userId, postId)).isTrue();
    }

    @Test
    void hasLiked_returnsFalseWhenNotLiked() {
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();

        when(likeRepository.existsByUserIdAndPostId(userId, postId)).thenReturn(false);

        assertThat(interactionService.hasLiked(userId, postId)).isFalse();
    }
}
