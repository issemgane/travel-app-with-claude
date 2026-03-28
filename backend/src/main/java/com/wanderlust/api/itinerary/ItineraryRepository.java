package com.wanderlust.api.itinerary;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ItineraryRepository extends JpaRepository<Itinerary, UUID> {

    Page<Itinerary> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Page<Itinerary> findByIsPublishedTrueOrderByCreatedAtDesc(Pageable pageable);
}
