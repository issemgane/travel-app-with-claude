package com.wanderlust.api.discovery;

import com.wanderlust.api.common.PagedResponse;
import com.wanderlust.api.post.PostDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/discover")
@RequiredArgsConstructor
@Tag(name = "Discovery")
public class DiscoveryController {

    private final DiscoveryService discoveryService;

    @GetMapping("/map")
    @Operation(summary = "Get posts within map bounding box")
    public ResponseEntity<PagedResponse<PostDto>> getMapPosts(
            @RequestParam double swLat,
            @RequestParam double swLng,
            @RequestParam double neLat,
            @RequestParam double neLng,
            Pageable pageable) {
        return ResponseEntity.ok(PagedResponse.from(
                discoveryService.getMapPosts(swLat, swLng, neLat, neLng, pageable)));
    }

    @GetMapping("/trending")
    @Operation(summary = "Get trending posts")
    public ResponseEntity<PagedResponse<PostDto>> getTrending(Pageable pageable) {
        return ResponseEntity.ok(PagedResponse.from(discoveryService.getTrending(pageable)));
    }

    @GetMapping("/destinations/{countryCode}")
    @Operation(summary = "Get destination summary")
    public ResponseEntity<DestinationSummaryDto> getDestination(@PathVariable String countryCode) {
        return ResponseEntity.ok(discoveryService.getDestinationSummary(countryCode));
    }
}
