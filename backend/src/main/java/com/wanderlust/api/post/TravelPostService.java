package com.wanderlust.api.post;

import com.wanderlust.api.common.ApiException;
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
public class TravelPostService {

    private final TravelPostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public PostDto create(UUID userId, CreatePostRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("User", userId));

        TravelPost post = TravelPost.builder()
                .user(user)
                .content(request.getContent())
                .category(request.getCategory() != null ? request.getCategory() : PostCategory.SPOT)
                .costLevel(request.getCostLevel())
                .bestSeason(request.getBestSeason())
                .durationSuggested(request.getDurationSuggested())
                .accessibilityRating(request.getAccessibilityRating())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .placeName(request.getPlaceName())
                .countryCode(request.getCountryCode().toUpperCase())
                .tags(request.getTags() != null ? String.join(",", request.getTags()) : null)
                .build();

        // Add media
        for (int i = 0; i < request.getMediaItems().size(); i++) {
            var item = request.getMediaItems().get(i);
            PostMedia media = PostMedia.builder()
                    .post(post)
                    .mediaUrl(item.getMediaUrl())
                    .mediaType(item.getMediaType() != null ? item.getMediaType() : MediaType.IMAGE)
                    .displayOrder(i)
                    .width(item.getWidth())
                    .height(item.getHeight())
                    .build();
            post.getMedia().add(media);
        }

        return PostDto.from(postRepository.save(post));
    }

    @Transactional(readOnly = true)
    public PostDto getById(UUID id) {
        TravelPost post = postRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Post", id));
        return PostDto.from(post);
    }

    @Transactional
    public void delete(UUID postId, UUID userId) {
        TravelPost post = postRepository.findById(postId)
                .orElseThrow(() -> ApiException.notFound("Post", postId));
        if (!post.getUser().getId().equals(userId)) {
            throw ApiException.forbidden("You can only delete your own posts");
        }
        postRepository.delete(post);
    }

    @Transactional(readOnly = true)
    public Page<PostDto> getFeed(UUID userId, Pageable pageable) {
        return postRepository.findFeedForUser(userId, pageable).map(PostDto::from);
    }

    @Transactional(readOnly = true)
    public Page<PostDto> getNearby(double lat, double lng, double radiusMeters, Pageable pageable) {
        return postRepository.findNearby(lat, lng, radiusMeters, pageable).map(PostDto::from);
    }

    @Transactional(readOnly = true)
    public Page<PostDto> getByDestination(String countryCode, Pageable pageable) {
        return postRepository.findByCountryCodeOrderByCreatedAtDesc(countryCode.toUpperCase(), pageable)
                .map(PostDto::from);
    }

    @Transactional(readOnly = true)
    public Page<PostDto> search(String query, Pageable pageable) {
        return postRepository.searchByContent(query, pageable).map(PostDto::from);
    }

    @Transactional(readOnly = true)
    public Page<PostDto> getByUser(UUID userId, Pageable pageable) {
        return postRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable).map(PostDto::from);
    }
}
