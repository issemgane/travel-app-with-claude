package com.wanderlust.api.bookmark;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface BookmarkRepository extends JpaRepository<Bookmark, BookmarkId> {

    @Query("SELECT b.id.postId FROM Bookmark b WHERE b.id.userId = :userId ORDER BY b.createdAt DESC")
    Page<UUID> findPostIdsByUserId(UUID userId, Pageable pageable);

    boolean existsById(BookmarkId id);
}
