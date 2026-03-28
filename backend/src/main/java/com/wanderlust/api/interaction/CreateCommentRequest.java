package com.wanderlust.api.interaction;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateCommentRequest {

    @NotBlank(message = "Comment content is required")
    @Size(max = 2000)
    private String content;

    private UUID parentId;

    private Boolean isQuestion = false;

    private Boolean isAnswer = false;
}
