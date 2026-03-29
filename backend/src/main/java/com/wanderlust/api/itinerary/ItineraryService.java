package com.wanderlust.api.itinerary;

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
public class ItineraryService {

    private final ItineraryRepository itineraryRepository;
    private final UserRepository userRepository;

    @Transactional
    public ItineraryDto create(UUID userId, CreateItineraryRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("User", userId));

        Itinerary itinerary = Itinerary.builder()
                .user(user)
                .title(request.getTitle())
                .description(request.getDescription())
                .countryCodes(request.getCountryCodes() != null ? String.join(",", request.getCountryCodes()) : null)
                .durationDays(request.getDurationDays())
                .estimatedBudgetUsd(request.getEstimatedBudgetUsd())
                .coverImageUrl(request.getCoverImageUrl())
                .isPublished(request.getIsPublished() != null && request.getIsPublished())
                .build();

        // Create empty days
        for (int i = 1; i <= request.getDurationDays(); i++) {
            ItineraryDay day = ItineraryDay.builder()
                    .itinerary(itinerary)
                    .dayNumber(i)
                    .title("Day " + i)
                    .build();
            itinerary.getDays().add(day);
        }

        return ItineraryDto.from(itineraryRepository.save(itinerary));
    }

    @Transactional(readOnly = true)
    public ItineraryDto getById(UUID id) {
        Itinerary itinerary = itineraryRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Itinerary", id));
        return ItineraryDto.from(itinerary);
    }

    @Transactional
    public void delete(UUID itineraryId, UUID userId) {
        Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> ApiException.notFound("Itinerary", itineraryId));
        if (!itinerary.getUser().getId().equals(userId)) {
            throw ApiException.forbidden("You can only delete your own itineraries");
        }
        itineraryRepository.delete(itinerary);
    }

    @Transactional
    public ItineraryDto clone(UUID itineraryId, UUID userId) {
        Itinerary original = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> ApiException.notFound("Itinerary", itineraryId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("User", userId));

        Itinerary cloned = Itinerary.builder()
                .user(user)
                .title(original.getTitle() + " (copy)")
                .description(original.getDescription())
                .countryCodes(original.getCountryCodes())
                .durationDays(original.getDurationDays())
                .estimatedBudgetUsd(original.getEstimatedBudgetUsd())
                .coverImageUrl(original.getCoverImageUrl())
                .isPublished(false)
                .clonedFrom(original.getId())
                .build();

        // Deep copy days and items
        for (ItineraryDay originalDay : original.getDays()) {
            ItineraryDay clonedDay = ItineraryDay.builder()
                    .itinerary(cloned)
                    .dayNumber(originalDay.getDayNumber())
                    .title(originalDay.getTitle())
                    .notes(originalDay.getNotes())
                    .build();

            for (ItineraryItem originalItem : originalDay.getItems()) {
                ItineraryItem clonedItem = ItineraryItem.builder()
                        .day(clonedDay)
                        .postId(originalItem.getPostId())
                        .customTitle(originalItem.getCustomTitle())
                        .customNote(originalItem.getCustomNote())
                        .transportToNext(originalItem.getTransportToNext())
                        .displayOrder(originalItem.getDisplayOrder())
                        .build();
                clonedDay.getItems().add(clonedItem);
            }
            cloned.getDays().add(clonedDay);
        }

        return ItineraryDto.from(itineraryRepository.save(cloned));
    }

    @Transactional(readOnly = true)
    public Page<ItineraryDto> getPublished(Pageable pageable) {
        return itineraryRepository.findByIsPublishedTrueOrderByCreatedAtDesc(pageable)
                .map(ItineraryDto::from);
    }

    @Transactional(readOnly = true)
    public Page<ItineraryDto> getByUser(UUID userId, Pageable pageable) {
        return itineraryRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(ItineraryDto::from);
    }
}
