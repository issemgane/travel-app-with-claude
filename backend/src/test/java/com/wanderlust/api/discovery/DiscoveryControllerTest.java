package com.wanderlust.api.discovery;

import com.wanderlust.api.common.PagedResponse;
import com.wanderlust.api.post.PostDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiscoveryControllerTest {

    @Mock
    private DiscoveryService discoveryService;

    @InjectMocks
    private DiscoveryController discoveryController;

    @Test
    void getMapPosts_returns200() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<PostDto> page = new PageImpl<>(List.of());
        when(discoveryService.getMapPosts(1.0, 2.0, 3.0, 4.0, pageable)).thenReturn(page);

        var response = discoveryController.getMapPosts(1.0, 2.0, 3.0, 4.0, pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void getTrending_returns200() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<PostDto> page = new PageImpl<>(List.of());
        when(discoveryService.getTrending(pageable)).thenReturn(page);

        var response = discoveryController.getTrending(pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getDestination_returns200() {
        var dto = DestinationSummaryDto.builder().countryCode("US").postCount(10).topPosts(List.of()).build();
        when(discoveryService.getDestinationSummary("US")).thenReturn(dto);

        var response = discoveryController.getDestination("US");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getCountryCode()).isEqualTo("US");
    }
}
