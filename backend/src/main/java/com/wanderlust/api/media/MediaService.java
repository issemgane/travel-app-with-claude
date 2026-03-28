package com.wanderlust.api.media;

import com.wanderlust.api.common.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MediaService {

    private final S3Presigner s3Presigner;

    @Value("${wanderlust.media.s3.bucket}")
    private String bucket;

    @Value("${wanderlust.media.cdn-base-url}")
    private String cdnBaseUrl;

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "video/mp4"
    );

    public PresignedUrlResponse generatePresignedUrl(String contentType, String filename) {
        if (!ALLOWED_TYPES.contains(contentType)) {
            throw ApiException.badRequest("Unsupported file type: " + contentType);
        }

        String extension = filename.substring(filename.lastIndexOf('.'));
        String key = "uploads/" + UUID.randomUUID() + extension;

        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();

        PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(builder ->
                builder.putObjectRequest(putRequest)
                        .signatureDuration(Duration.ofMinutes(15)));

        return PresignedUrlResponse.builder()
                .uploadUrl(presigned.url().toString())
                .mediaUrl(cdnBaseUrl + "/" + key)
                .key(key)
                .build();
    }
}
