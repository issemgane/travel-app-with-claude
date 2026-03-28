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
            WHERE ST_DWithin(tp.location, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography, :radius)
            ORDER BY tp.location <-> ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography
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
            WHERE to_tsvector('english', COALESCE(tp.content, '') || ' ' || tp.place_name)
                  @@ plainto_tsquery('english', :query)
            ORDER BY ts_rank(
                to_tsvector('english', COALESCE(tp.content, '') || ' ' || tp.place_name),
                plainto_tsquery('english', :query)
            ) DESC
            """, nativeQuery = true)
    Page<TravelPost> searchByContent(@Param("query") String query, Pageable pageable);

    @Query(value = """
            SELECT tp.* FROM travel_posts tp
            WHERE ST_Within(
                tp.location::geometry,
                ST_MakeEnvelope(:swLng, :swLat, :neLng, :neLat, 4326)
            )
            ORDER BY tp.likes_count DESC
            """, nativeQuery = true)
    Page<TravelPost> findWithinBounds(
            @Param("swLat") double swLat, @Param("swLng") double swLng,
            @Param("neLat") double neLat, @Param("neLng") double neLng,
            Pageable pageable);

    @Query(value = """
            SELECT tp.* FROM travel_posts tp
            WHERE tp.created_at > NOW() - INTERVAL '7 days'
            ORDER BY tp.likes_count DESC
            """, nativeQuery = true)
    Page<TravelPost> findTrending(Pageable pageable);

    long countByUserId(UUID userId);

    long countByCountryCode(String countryCode);
}
