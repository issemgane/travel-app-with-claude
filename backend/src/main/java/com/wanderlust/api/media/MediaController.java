package com.wanderlust.api.media;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
@Tag(name = "Media")
public class MediaController {

    private final MediaService mediaService;

    @PostMapping("/presigned-url")
    @Operation(summary = "Get presigned upload URL for media")
    public ResponseEntity<PresignedUrlResponse> getPresignedUrl(
            @Valid @RequestBody PresignedUrlRequest request) {
        return ResponseEntity.ok(
                mediaService.generatePresignedUrl(request.getContentType(), request.getFilename()));
    }
}
