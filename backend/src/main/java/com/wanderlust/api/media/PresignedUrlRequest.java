package com.wanderlust.api.media;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PresignedUrlRequest {

    @NotBlank(message = "Content type is required")
    private String contentType;

    @NotBlank(message = "Filename is required")
    private String filename;
}
