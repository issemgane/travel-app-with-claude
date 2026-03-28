package com.wanderlust.api.discovery;

import com.wanderlust.api.post.PostDto;
import com.wanderlust.api.post.TravelPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DiscoveryService {

    private final TravelPostRepository postRepository;

    @Transactional(readOnly = true)
    public Page<PostDto> getMapPosts(double swLat, double swLng, double neLat, double neLng, Pageable pageable) {
        return postRepository.findWithinBounds(swLat, swLng, neLat, neLng, pageable)
                .map(PostDto::from);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "trending")
    public Page<PostDto> getTrending(Pageable pageable) {
        return postRepository.findTrending(pageable).map(PostDto::from);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "destinations", key = "#countryCode")
    public DestinationSummaryDto getDestinationSummary(String countryCode) {
        String code = countryCode.toUpperCase();
        long postCount = postRepository.countByCountryCode(code);

        var topPosts = postRepository.findByCountryCodeOrderByCreatedAtDesc(code, PageRequest.of(0, 10))
                .map(PostDto::from)
                .getContent();

        return DestinationSummaryDto.builder()
                .countryCode(code)
                .postCount(postCount)
                .topPosts(topPosts)
                .build();
    }
}
