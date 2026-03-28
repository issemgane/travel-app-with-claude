package com.wanderlust.api.interaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {
    private UUID id;
    private UUID postId;
    private UUID parentId;
    private UUID userId;
    private String username;
    private String displayName;
    private String avatarUrl;
    private String content;
    private Boolean isQuestion;
    private Boolean isAnswer;
    private Instant createdAt;

    public static CommentDto from(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .postId(comment.getPostId())
                .parentId(comment.getParentId())
                .userId(comment.getUser().getId())
                .username(comment.getUser().getUsername())
                .displayName(comment.getUser().getDisplayName())
                .avatarUrl(comment.getUser().getAvatarUrl())
                .content(comment.getContent())
                .isQuestion(comment.getIsQuestion())
                .isAnswer(comment.getIsAnswer())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
