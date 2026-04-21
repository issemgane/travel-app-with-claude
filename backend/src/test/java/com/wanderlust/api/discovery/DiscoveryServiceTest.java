package com.wanderlust.api.discovery;

import com.wanderlust.api.post.PostDto;
import com.wanderlust.api.post.TravelPost;
import com.wanderlust.api.post.TravelPostRepository;
import com.wanderlust.api.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiscoveryServiceTest {

    @Mock
    private TravelPostRepository postRepository;

    @InjectMocks
    private DiscoveryService discoveryService;

    private TravelPost createPost() {
        User user = User.builder().username("test").displayName("Test").email("t@t.com").passwordHash("h").build();
        user.setId(UUID.randomUUID());
        TravelPost post = TravelPost.builder().user(user).content("content").placeName("Place")
                .countryCode("US").latitude(0.0).longitude(0.0).media(new ArrayList<>()).build();
        post.setId(UUID.randomUUID());
        return post;
    }

    @Test
    void getMapPosts_delegatesToRepository() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<TravelPost> page = new PageImpl<>(List.of(createPost()));
        when(postRepository.findWithinBounds(1.0, 2.0, 3.0, 4.0, pageable)).thenReturn(page);

        Page<PostDto> result = discoveryService.getMapPosts(1.0, 2.0, 3.0, 4.0, pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(postRepository).findWithinBounds(1.0, 2.0, 3.0, 4.0, pageable);
    }

    @Test
    void getTrending_delegatesToRepository() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<TravelPost> page = new PageImpl<>(List.of(createPost()));
        when(postRepository.findTrending(pageable)).thenReturn(page);

        Page<PostDto> result = discoveryService.getTrending(pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(postRepository).findTrending(pageable);
    }

    @Test
    void getDestinationSummary_uppercasesCountryCode() {
        when(postRepository.countByCountryCode("FR")).thenReturn(5L);
        when(postRepository.findByCountryCodeOrderByCreatedAtDesc(eq("FR"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(createPost())));

        DestinationSummaryDto result = discoveryService.getDestinationSummary("fr");

        assertThat(result.getCountryCode()).isEqualTo("FR");
        assertThat(result.getPostCount()).isEqualTo(5L);
        assertThat(result.getTopPosts()).hasSize(1);
    }

    @Test
    void getDestinationSummary_returnsCorrectPostCount() {
        when(postRepository.countByCountryCode("US")).thenReturn(42L);
        when(postRepository.findByCountryCodeOrderByCreatedAtDesc(eq("US"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        DestinationSummaryDto result = discoveryService.getDestinationSummary("US");

        assertThat(result.getPostCount()).isEqualTo(42L);
        assertThat(result.getTopPosts()).isEmpty();
    }
}
