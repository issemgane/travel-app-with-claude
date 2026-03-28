package com.wanderlust.api.itinerary;

import com.wanderlust.api.common.PagedResponse;
import com.wanderlust.api.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/itineraries")
@RequiredArgsConstructor
@Tag(name = "Itineraries")
public class ItineraryController {

    private final ItineraryService itineraryService;
    private final UserService userService;

    @PostMapping
    @Operation(summary = "Create an itinerary")
    public ResponseEntity<ItineraryDto> create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateItineraryRequest request) {
        UUID userId = userService.resolveUserId(jwt);
        return ResponseEntity.status(HttpStatus.CREATED).body(itineraryService.create(userId, request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get itinerary by ID")
    public ResponseEntity<ItineraryDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(itineraryService.getById(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete own itinerary")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id) {
        UUID userId = userService.resolveUserId(jwt);
        itineraryService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/clone")
    @Operation(summary = "Clone an itinerary")
    public ResponseEntity<ItineraryDto> clone(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id) {
        UUID userId = userService.resolveUserId(jwt);
        return ResponseEntity.status(HttpStatus.CREATED).body(itineraryService.clone(id, userId));
    }

    @GetMapping
    @Operation(summary = "Get published itineraries")
    public ResponseEntity<PagedResponse<ItineraryDto>> getPublished(Pageable pageable) {
        return ResponseEntity.ok(PagedResponse.from(itineraryService.getPublished(pageable)));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get itineraries by user")
    public ResponseEntity<PagedResponse<ItineraryDto>> getByUser(
            @PathVariable UUID userId,
            Pageable pageable) {
        return ResponseEntity.ok(PagedResponse.from(itineraryService.getByUser(userId, pageable)));
    }
}
