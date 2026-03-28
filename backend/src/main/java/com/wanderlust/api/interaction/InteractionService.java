package com.wanderlust.api.interaction;

import com.wanderlust.api.common.ApiException;
import com.wanderlust.api.post.TravelPostRepository;
import com.wanderlust.api.user.User;
import com.wanderlust.api.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InteractionService {

    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final TravelPostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public boolean toggleLike(UUID userId, UUID postId) {
        if (!postRepository.existsById(postId)) {
            throw ApiException.notFound("Post", postId);
        }

        if (likeRepository.existsByUserIdAndPostId(userId, postId)) {
            likeRepository.deleteByUserIdAndPostId(userId, postId);
            return false; // unliked
        } else {
            likeRepository.save(Like.builder().userId(userId).postId(postId).build());
            return true; // liked
        }
    }

    @Transactional
    public CommentDto addComment(UUID userId, UUID postId, CreateCommentRequest request) {
        if (!postRepository.existsById(postId)) {
            throw ApiException.notFound("Post", postId);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("User", userId));

        Comment comment = Comment.builder()
                .postId(postId)
                .user(user)
                .parentId(request.getParentId())
                .content(request.getContent())
                .isQuestion(request.getIsQuestion() != null && request.getIsQuestion())
                .isAnswer(request.getIsAnswer() != null && request.getIsAnswer())
                .build();

        return CommentDto.from(commentRepository.save(comment));
    }

    @Transactional(readOnly = true)
    public Page<CommentDto> getComments(UUID postId, Pageable pageable) {
        return commentRepository.findByPostIdOrderByCreatedAtDesc(postId, pageable)
                .map(CommentDto::from);
    }

    @Transactional(readOnly = true)
    public Page<CommentDto> getQuestions(UUID postId, Pageable pageable) {
        return commentRepository.findByPostIdAndIsQuestionTrueOrderByCreatedAtDesc(postId, pageable)
                .map(CommentDto::from);
    }

    @Transactional(readOnly = true)
    public boolean hasLiked(UUID userId, UUID postId) {
        return likeRepository.existsByUserIdAndPostId(userId, postId);
    }
}
