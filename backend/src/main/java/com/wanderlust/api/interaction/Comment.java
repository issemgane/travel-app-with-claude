package com.wanderlust.api.interaction;

import com.wanderlust.api.common.BaseEntity;
import com.wanderlust.api.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "comments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment extends BaseEntity {

    @Column(name = "post_id", nullable = false)
    private UUID postId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "parent_id")
    private UUID parentId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_question", nullable = false)
    @Builder.Default
    private Boolean isQuestion = false;

    @Column(name = "is_answer", nullable = false)
    @Builder.Default
    private Boolean isAnswer = false;
}
