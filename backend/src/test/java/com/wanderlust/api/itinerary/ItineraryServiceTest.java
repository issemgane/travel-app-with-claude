package com.wanderlust.api.itinerary;

import com.wanderlust.api.common.ApiException;
import com.wanderlust.api.user.User;
import com.wanderlust.api.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItineraryServiceTest {

    @Mock
    private ItineraryRepository itineraryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ItineraryService itineraryService;

    private UUID userId;
    private UUID itineraryId;
    private User user;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        itineraryId = UUID.randomUUID();
        user = User.builder()
                .username("traveler")
                .displayName("World Traveler")
                .email("traveler@example.com")
                .passwordHash("hashed")
                .build();
        user.setId(userId);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
    }

    private Itinerary buildItinerary(UUID id, User owner, String title, int durationDays) {
        Itinerary itinerary = Itinerary.builder()
                .user(owner)
                .title(title)
                .description("A great trip")
                .countryCodes("JP,TH")
                .durationDays(durationDays)
                .estimatedBudgetUsd(new BigDecimal("2000.00"))
                .coverImageUrl("https://example.com/cover.jpg")
                .isPublished(false)
                .build();
        itinerary.setId(id);
        itinerary.setCreatedAt(Instant.now());
        itinerary.setUpdatedAt(Instant.now());
        return itinerary;
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("saves itinerary with correct number of days")
        void savesItineraryWithCorrectDaysCount() {
            CreateItineraryRequest request = new CreateItineraryRequest();
            request.setTitle("Japan Trip");
            request.setDescription("Exploring Japan");
            request.setCountryCodes(new String[]{"JP"});
            request.setDurationDays(3);
            request.setEstimatedBudgetUsd(new BigDecimal("3000.00"));
            request.setCoverImageUrl("https://example.com/japan.jpg");
            request.setIsPublished(false);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(itineraryRepository.save(any(Itinerary.class))).thenAnswer(inv -> {
                Itinerary saved = inv.getArgument(0);
                saved.setId(UUID.randomUUID());
                saved.setCreatedAt(Instant.now());
                saved.setUpdatedAt(Instant.now());
                return saved;
            });

            ItineraryDto result = itineraryService.create(userId, request);

            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("Japan Trip");
            assertThat(result.getDays()).hasSize(3);
            assertThat(result.getDays().get(0).getDayNumber()).isEqualTo(1);
            assertThat(result.getDays().get(0).getTitle()).isEqualTo("Day 1");
            assertThat(result.getDays().get(1).getDayNumber()).isEqualTo(2);
            assertThat(result.getDays().get(2).getDayNumber()).isEqualTo(3);

            ArgumentCaptor<Itinerary> captor = ArgumentCaptor.forClass(Itinerary.class);
            verify(itineraryRepository).save(captor.capture());
            Itinerary saved = captor.getValue();
            assertThat(saved.getUser()).isEqualTo(user);
            assertThat(saved.getDurationDays()).isEqualTo(3);
            assertThat(saved.getCountryCodes()).isEqualTo("JP");
            assertThat(saved.getDays()).hasSize(3);
        }

        @Test
        @DisplayName("throws NOT_FOUND when user not found")
        void throwsWhenUserNotFound() {
            CreateItineraryRequest request = new CreateItineraryRequest();
            request.setTitle("Trip");
            request.setDurationDays(1);

            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> itineraryService.create(userId, request))
                    .isInstanceOf(ApiException.class)
                    .satisfies(ex -> {
                        ApiException apiEx = (ApiException) ex;
                        assertThat(apiEx.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    });

            verify(itineraryRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("returns ItineraryDto when found")
        void returnsItineraryDtoWhenFound() {
            Itinerary itinerary = buildItinerary(itineraryId, user, "Japan Trip", 2);

            when(itineraryRepository.findById(itineraryId)).thenReturn(Optional.of(itinerary));

            ItineraryDto result = itineraryService.getById(itineraryId);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(itineraryId);
            assertThat(result.getTitle()).isEqualTo("Japan Trip");
            assertThat(result.getUserId()).isEqualTo(userId);
            verify(itineraryRepository).findById(itineraryId);
        }

        @Test
        @DisplayName("throws NOT_FOUND when not found")
        void throwsWhenNotFound() {
            when(itineraryRepository.findById(itineraryId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> itineraryService.getById(itineraryId))
                    .isInstanceOf(ApiException.class)
                    .satisfies(ex -> {
                        ApiException apiEx = (ApiException) ex;
                        assertThat(apiEx.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    });

            verify(itineraryRepository).findById(itineraryId);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("deletes when user is the owner")
        void deletesWhenOwner() {
            Itinerary itinerary = buildItinerary(itineraryId, user, "My Trip", 2);

            when(itineraryRepository.findById(itineraryId)).thenReturn(Optional.of(itinerary));

            itineraryService.delete(itineraryId, userId);

            verify(itineraryRepository).delete(itinerary);
        }

        @Test
        @DisplayName("throws FORBIDDEN when user is not the owner")
        void throwsForbiddenWhenNotOwner() {
            UUID otherUserId = UUID.randomUUID();
            Itinerary itinerary = buildItinerary(itineraryId, user, "My Trip", 2);

            when(itineraryRepository.findById(itineraryId)).thenReturn(Optional.of(itinerary));

            assertThatThrownBy(() -> itineraryService.delete(itineraryId, otherUserId))
                    .isInstanceOf(ApiException.class)
                    .satisfies(ex -> {
                        ApiException apiEx = (ApiException) ex;
                        assertThat(apiEx.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
                    });

            verify(itineraryRepository, never()).delete(any());
        }

        @Test
        @DisplayName("throws NOT_FOUND when itinerary not found")
        void throwsNotFoundWhenItineraryNotFound() {
            when(itineraryRepository.findById(itineraryId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> itineraryService.delete(itineraryId, userId))
                    .isInstanceOf(ApiException.class)
                    .satisfies(ex -> {
                        ApiException apiEx = (ApiException) ex;
                        assertThat(apiEx.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    });

            verify(itineraryRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("clone")
    class Clone {

        @Test
        @DisplayName("deep clones itinerary with ' (copy)' suffix and isPublished=false")
        void deepClonesWithCopySuffix() {
            Itinerary original = buildItinerary(itineraryId, user, "Original Trip", 2);

            // Add days with items to the original
            ItineraryDay day1 = ItineraryDay.builder()
                    .itinerary(original)
                    .dayNumber(1)
                    .title("Day 1")
                    .notes("Visit temple")
                    .build();
            day1.setId(UUID.randomUUID());

            ItineraryItem item1 = ItineraryItem.builder()
                    .day(day1)
                    .postId(UUID.randomUUID())
                    .customTitle("Temple Visit")
                    .customNote("Morning visit")
                    .transportToNext("taxi")
                    .displayOrder(0)
                    .build();
            item1.setId(UUID.randomUUID());
            day1.getItems().add(item1);
            original.getDays().add(day1);

            UUID cloneUserId = UUID.randomUUID();
            User cloneUser = User.builder()
                    .username("cloner")
                    .displayName("Cloner")
                    .email("cloner@example.com")
                    .passwordHash("hashed")
                    .build();
            cloneUser.setId(cloneUserId);
            cloneUser.setCreatedAt(Instant.now());
            cloneUser.setUpdatedAt(Instant.now());

            when(itineraryRepository.findById(itineraryId)).thenReturn(Optional.of(original));
            when(userRepository.findById(cloneUserId)).thenReturn(Optional.of(cloneUser));
            when(itineraryRepository.save(any(Itinerary.class))).thenAnswer(inv -> {
                Itinerary saved = inv.getArgument(0);
                saved.setId(UUID.randomUUID());
                saved.setCreatedAt(Instant.now());
                saved.setUpdatedAt(Instant.now());
                return saved;
            });

            ItineraryDto result = itineraryService.clone(itineraryId, cloneUserId);

            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("Original Trip (copy)");
            assertThat(result.getIsPublished()).isFalse();
            assertThat(result.getUserId()).isEqualTo(cloneUserId);
            assertThat(result.getClonedFrom()).isEqualTo(itineraryId);

            ArgumentCaptor<Itinerary> captor = ArgumentCaptor.forClass(Itinerary.class);
            verify(itineraryRepository).save(captor.capture());
            Itinerary cloned = captor.getValue();
            assertThat(cloned.getDays()).hasSize(1);
            assertThat(cloned.getDays().get(0).getTitle()).isEqualTo("Day 1");
            assertThat(cloned.getDays().get(0).getNotes()).isEqualTo("Visit temple");
            assertThat(cloned.getDays().get(0).getItems()).hasSize(1);
            assertThat(cloned.getDays().get(0).getItems().get(0).getCustomTitle()).isEqualTo("Temple Visit");
            assertThat(cloned.getDays().get(0).getItems().get(0).getTransportToNext()).isEqualTo("taxi");
        }

        @Test
        @DisplayName("throws NOT_FOUND when itinerary not found")
        void throwsNotFoundWhenItineraryNotFound() {
            when(itineraryRepository.findById(itineraryId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> itineraryService.clone(itineraryId, userId))
                    .isInstanceOf(ApiException.class)
                    .satisfies(ex -> {
                        ApiException apiEx = (ApiException) ex;
                        assertThat(apiEx.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    });

            verify(itineraryRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws NOT_FOUND when user not found")
        void throwsNotFoundWhenUserNotFound() {
            Itinerary original = buildItinerary(itineraryId, user, "Trip", 1);

            when(itineraryRepository.findById(itineraryId)).thenReturn(Optional.of(original));
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> itineraryService.clone(itineraryId, userId))
                    .isInstanceOf(ApiException.class)
                    .satisfies(ex -> {
                        ApiException apiEx = (ApiException) ex;
                        assertThat(apiEx.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    });

            verify(itineraryRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getPublished")
    class GetPublished {

        @Test
        @DisplayName("delegates to repository and returns mapped page")
        void delegatesToRepository() {
            Pageable pageable = PageRequest.of(0, 10);
            Itinerary itinerary = buildItinerary(itineraryId, user, "Published Trip", 3);
            itinerary.setIsPublished(true);
            Page<Itinerary> page = new PageImpl<>(List.of(itinerary), pageable, 1);

            when(itineraryRepository.findByIsPublishedTrueOrderByCreatedAtDesc(pageable)).thenReturn(page);

            Page<ItineraryDto> result = itineraryService.getPublished(pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getTitle()).isEqualTo("Published Trip");
            assertThat(result.getContent().get(0).getIsPublished()).isTrue();
            verify(itineraryRepository).findByIsPublishedTrueOrderByCreatedAtDesc(pageable);
        }
    }

    @Nested
    @DisplayName("getByUser")
    class GetByUser {

        @Test
        @DisplayName("delegates to repository and returns mapped page")
        void delegatesToRepository() {
            Pageable pageable = PageRequest.of(0, 10);
            Itinerary itinerary = buildItinerary(itineraryId, user, "User Trip", 2);
            Page<Itinerary> page = new PageImpl<>(List.of(itinerary), pageable, 1);

            when(itineraryRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)).thenReturn(page);

            Page<ItineraryDto> result = itineraryService.getByUser(userId, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getTitle()).isEqualTo("User Trip");
            assertThat(result.getContent().get(0).getUserId()).isEqualTo(userId);
            verify(itineraryRepository).findByUserIdOrderByCreatedAtDesc(userId, pageable);
        }
    }
}
