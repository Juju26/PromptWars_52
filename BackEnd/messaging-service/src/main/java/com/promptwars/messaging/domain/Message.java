package com.promptwars.messaging.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Message entity — immutable once created (soft-delete via deletedAt).
 */
@Entity
@Table(name = "messages", indexes = {
        @Index(name = "idx_messages_channel_id", columnList = "channel_id, created_at DESC"),
        @Index(name = "idx_messages_sender", columnList = "sender_id"),
        @Index(name = "idx_messages_deleted", columnList = "deleted_at") // partial index for soft-delete filtering
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "channel_id", nullable = false)
    private UUID channelId;

    @Column(name = "sender_id", nullable = false)
    private UUID senderId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "sender_username", length = 50)
    private String senderUsername;

    @Column(name = "deleted_at")
    private Instant deletedAt;  // Soft delete for audit trail

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
