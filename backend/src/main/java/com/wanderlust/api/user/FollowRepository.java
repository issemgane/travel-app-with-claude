package com.wanderlust.api.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface FollowRepository extends JpaRepository<Follow, FollowId> {

    boolean existsById(FollowId id);

    long countByIdFollowerId(UUID followerId);

    long countByIdFollowingId(UUID followingId);

    @Query("SELECT f.id.followerId FROM Follow f WHERE f.id.followingId = :userId")
    Page<UUID> findFollowerIds(UUID userId, Pageable pageable);

    @Query("SELECT f.id.followingId FROM Follow f WHERE f.id.followerId = :userId")
    Page<UUID> findFollowingIds(UUID userId, Pageable pageable);

    default long countByFollowerId(UUID followerId) {
        return countByIdFollowerId(followerId);
    }

    default long countByFollowingId(UUID followingId) {
        return countByIdFollowingId(followingId);
    }
}
