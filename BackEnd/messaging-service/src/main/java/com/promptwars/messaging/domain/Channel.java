package com.promptwars.messaging.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Channel entity — represents a Slack-like room (team channel or announcement).
 */
@Entity
@Table(name = "channels", indexes = {
        @Index(name = "idx_channels_team_id", columnList = "team_id"),
        @Index(name = "idx_channels_type", columnList = "type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Channel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ChannelType type = ChannelType.TEAM;

    @Column(name = "team_id")
    private UUID teamId;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    public enum ChannelType {
        TEAM,          // Private to a team
        ANNOUNCEMENT,  // Organiser-only send, all receive
        GENERAL        // Open to all participants
    }
}
