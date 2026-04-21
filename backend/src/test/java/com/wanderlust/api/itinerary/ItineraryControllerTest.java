package com.wanderlust.api.itinerary;

import com.wanderlust.api.common.PagedResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItineraryControllerTest {

    @Mock
    private ItineraryService itineraryService;

    @InjectMocks
    private ItineraryController itineraryController;

    private UUID userId;
    private UUID itineraryId;
    private ItineraryDto sampleDto;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        itineraryId = UUID.randomUUID();
        sampleDto = ItineraryDto.builder()
                .id(itineraryId)
                .userId(userId)
                .username("traveler")
                .title("Japan Trip")
                .description("Exploring Japan")
                .countryCodes(new String[]{"JP"})
                .durationDays(3)
                .estimatedBudgetUsd(new BigDecimal("3000.00"))
                .coverImageUrl("https://example.com/japan.jpg")
                .isPublished(false)
                .days(List.of())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("returns 201 with created itinerary")
        void returns201WithCreatedItinerary() {
            CreateItineraryRequest request = new CreateItineraryRequest();
            request.setTitle("Japan Trip");
            request.setDurationDays(3);

            when(itineraryService.create(eq(userId), any(CreateItineraryRequest.class))).thenReturn(sampleDto);

            ResponseEntity<ItineraryDto> response = itineraryController.create(userId, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTitle()).isEqualTo("Japan Trip");
            assertThat(response.getBody().getId()).isEqualTo(itineraryId);
            verify(itineraryService).create(eq(userId), any(CreateItineraryRequest.class));
        }
    }

    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("returns 200 with itinerary")
        void returns200WithItinerary() {
            when(itineraryService.getById(itineraryId)).thenReturn(sampleDto);

            ResponseEntity<ItineraryDto> response = itineraryController.getById(itineraryId);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getId()).isEqualTo(itineraryId);
            verify(itineraryService).getById(itineraryId);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("returns 204")
        void returns204() {
            ResponseEntity<Void> response = itineraryController.delete(userId, itineraryId);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            verify(itineraryService).delete(itineraryId, userId);
        }
    }

    @Nested
    @DisplayName("clone")
    class CloneItinerary {

        @Test
        @DisplayName("returns 201 with cloned itinerary")
        void returns201WithClonedItinerary() {
            ItineraryDto clonedDto = ItineraryDto.builder()
                    .id(UUID.randomUUID())
                    .userId(userId)
                    .username("traveler")
                    .title("Japan Trip (copy)")
                    .isPublished(false)
                    .clonedFrom(itineraryId)
                    .days(List.of())
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();

            when(itineraryService.clone(itineraryId, userId)).thenReturn(clonedDto);

            ResponseEntity<ItineraryDto> response = itineraryController.clone(userId, itineraryId);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTitle()).isEqualTo("Japan Trip (copy)");
            assertThat(response.getBody().getClonedFrom()).isEqualTo(itineraryId);
            verify(itineraryService).clone(itineraryId, userId);
        }
    }

    @Nested
    @DisplayName("getPublished")
    class GetPublished {

        @Test
        @DisplayName("returns 200 with PagedResponse")
        void returns200WithPagedResponse() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<ItineraryDto> page = new PageImpl<>(List.of(sampleDto), pageable, 1);

            when(itineraryService.getPublished(pageable)).thenReturn(page);

            ResponseEntity<PagedResponse<ItineraryDto>> response = itineraryController.getPublished(pageable);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).hasSize(1);
            assertThat(response.getBody().getTotalElements()).isEqualTo(1);
            verify(itineraryService).getPublished(pageable);
        }
    }

    @Nested
    @DisplayName("getByUser")
    class GetByUser {

        @Test
        @DisplayName("returns 200 with PagedResponse")
        void returns200WithPagedResponse() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<ItineraryDto> page = new PageImpl<>(List.of(sampleDto), pageable, 1);

            when(itineraryService.getByUser(userId, pageable)).thenReturn(page);

            ResponseEntity<PagedResponse<ItineraryDto>> response = itineraryController.getByUser(userId, pageable);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).hasSize(1);
            assertThat(response.getBody().getContent().get(0).getUserId()).isEqualTo(userId);
            verify(itineraryService).getByUser(userId, pageable);
        }
    }
}
