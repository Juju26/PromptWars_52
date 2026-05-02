package com.promptwars.beacon.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * BeaconRegistration — maps a userId to their current local LAN IP + port.
 * TTL is enforced by the scheduled eviction job; lastSeen tracks activity.
 *
 * Design: one row per user. Upsert semantics on register (replace on conflict).
 */
@Entity
@Table(name = "beacon_registrations", indexes = {
        @Index(name = "idx_beacon_user_id",   columnList = "user_id",    unique = true),
        @Index(name = "idx_beacon_last_seen", columnList = "last_seen"),
        @Index(name = "idx_beacon_team_id",   columnList = "team_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BeaconRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "username", length = 50)
    private String username;

    @Column(name = "local_ip", nullable = false, length = 45) // Supports IPv6
    private String localIp;

    @Column(name = "daemon_port", nullable = false)
    private int daemonPort;

    @Column(name = "team_id")
    private UUID teamId;

    @Column(name = "last_seen", nullable = false)
    @UpdateTimestamp
    private Instant lastSeen;

    @Column(name = "registered_at", updatable = false)
    @Builder.Default
    private Instant registeredAt = Instant.now();
}
