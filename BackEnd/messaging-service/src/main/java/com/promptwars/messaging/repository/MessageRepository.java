package com.promptwars.messaging.repository;

import com.promptwars.messaging.domain.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

    /**
     * Cursor-based pagination using createdAt — avoids OFFSET for large channels.
     * Returns messages older than 'before' cursor, newest-first.
     */
    @Query("""
            SELECT m FROM Message m
            WHERE m.channelId = :channelId
              AND m.deletedAt IS NULL
              AND (:before IS NULL OR m.createdAt < :before)
            ORDER BY m.createdAt DESC
            """)
    List<Message> findByChannelIdCursor(UUID channelId, Instant before, Pageable pageable);

    /**
     * Standard paginated fallback (newest messages first).
     */
    Page<Message> findByChannelIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID channelId, Pageable pageable);

    long countByChannelIdAndDeletedAtIsNull(UUID channelId);
}
