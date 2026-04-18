package com.wanderlust.api.post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface TravelPostRepository extends JpaRepository<TravelPost, UUID> {

    Page<TravelPost> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Page<TravelPost> findByCountryCodeOrderByCreatedAtDesc(String countryCode, Pageable pageable);

    Page<TravelPost> findByCategoryOrderByCreatedAtDesc(PostCategory category, Pageable pageable);

    @Query(value = """
            SELECT tp.* FROM travel_posts tp
            WHERE (6371000 * acos(cos(radians(:lat)) * cos(radians(tp.latitude))
                * cos(radians(tp.longitude) - radians(:lng))
                + sin(radians(:lat)) * sin(radians(tp.latitude)))) < :radius
            ORDER BY (6371000 * acos(cos(radians(:lat)) * cos(radians(tp.latitude))
                * cos(radians(tp.longitude) - radians(:lng))
                + sin(radians(:lat)) * sin(radians(tp.latitude)))) ASC
            """, nativeQuery = true)
    Page<TravelPost> findNearby(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("radius") double radiusMeters,
            Pageable pageable);

    @Query(value = """
            SELECT tp.* FROM travel_posts tp
            INNER JOIN follows f ON f.following_id = tp.user_id
            WHERE f.follower_id = :userId
            ORDER BY tp.created_at DESC
            """, nativeQuery = true)
    Page<TravelPost> findFeedForUser(@Param("userId") UUID userId, Pageable pageable);

    @Query(value = """
            SELECT tp.* FROM travel_posts tp
            WHERE LOWER(COALESCE(tp.content, '') || ' ' || tp.place_name)
                  LIKE LOWER(CONCAT('%%', :query, '%%'))
            ORDER BY tp.created_at DESC
            """, nativeQuery = true)
    Page<TravelPost> searchByContent(@Param("query") String query, Pageable pageable);

    @Query(value = """
            SELECT tp.* FROM travel_posts tp
            WHERE tp.latitude BETWEEN :swLat AND :neLat
            AND tp.longitude BETWEEN :swLng AND :neLng
            ORDER BY tp.likes_count DESC
            """, nativeQuery = true)
    Page<TravelPost> findWithinBounds(
            @Param("swLat") double swLat, @Param("swLng") double swLng,
            @Param("neLat") double neLat, @Param("neLng") double neLng,
            Pageable pageable);

    @Query(value = """
            SELECT tp.* FROM travel_posts tp
            ORDER BY (tp.likes_count * 2 + tp.comments_count) DESC, tp.created_at DESC
            """, nativeQuery = true)
    Page<TravelPost> findTrending(Pageable pageable);

    long countByUserId(UUID userId);

    long countByCountryCode(String countryCode);
}
