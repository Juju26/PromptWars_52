package com.promptwars.beacon.repository;

import com.promptwars.beacon.domain.BeaconRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BeaconRepository extends JpaRepository<BeaconRegistration, UUID> {

    Optional<BeaconRegistration> findByUserId(UUID userId);

    /**
     * Active peers: only return registrations seen within TTL window.
     * Used by daemon to build its peer connection table.
     */
    @Query("SELECT b FROM BeaconRegistration b WHERE b.lastSeen >= :since ORDER BY b.username")
    List<BeaconRegistration> findActiveSince(Instant since);

    /**
     * Team-specific discovery — peers on the same team.
     */
    @Query("SELECT b FROM BeaconRegistration b WHERE b.teamId = :teamId AND b.lastSeen >= :since")
    List<BeaconRegistration> findActiveByTeam(UUID teamId, Instant since);

    /**
     * TTL eviction — bulk delete stale registrations.
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM BeaconRegistration b WHERE b.lastSeen < :cutoff")
    int deleteStaleRegistrations(Instant cutoff);

    boolean existsByUserId(UUID userId);
}
