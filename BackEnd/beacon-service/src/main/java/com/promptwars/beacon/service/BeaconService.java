package com.promptwars.beacon.service;

import com.promptwars.beacon.domain.BeaconRegistration;
import com.promptwars.beacon.dto.BeaconDtos;
import com.promptwars.beacon.repository.BeaconRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Beacon Registry Service — the fallback P2P peer discovery mechanism.
 *
 * Flow:
 *  1. Daemon calls POST /register to announce its LAN IP while online.
 *  2. Daemon calls GET /peers to fetch the current active peer table.
 *  3. Scheduled eviction removes stale entries (TTL = configurable minutes).
 *  4. Results are cached in-memory (Caffeine/ConcurrentMap) to handle
 *     the 500-user burst without hammering the DB on every daemon heartbeat.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BeaconService {

    private final BeaconRepository beaconRepository;

    @Value("${beacon.ttl-minutes:10}")
    private long ttlMinutes;

    // ── Registration ──────────────────────────────────────────────

    @Transactional
    @CacheEvict(value = "activePeers", allEntries = true)
    public BeaconDtos.BeaconResponse register(
            BeaconDtos.RegisterRequest request,
            UUID userId,
            String username) {

        BeaconRegistration reg = beaconRepository.findByUserId(userId)
                .orElseGet(() -> BeaconRegistration.builder()
                        .userId(userId)
                        .registeredAt(Instant.now())
                        .build());

        reg.setUsername(username);
        reg.setLocalIp(request.localIp());
        reg.setDaemonPort(request.daemonPort());
        reg.setTeamId(request.teamId());
        reg.setLastSeen(Instant.now());

        reg = beaconRepository.save(reg);
        log.info("Beacon registered: userId={} ip={}:{}", userId, request.localIp(), request.daemonPort());
        return toResponse(reg);
    }

    // ── Heartbeat (cheap upsert of lastSeen) ──────────────────────

    @Transactional
    @CacheEvict(value = "activePeers", allEntries = true)
    public void heartbeat(UUID userId) {
        beaconRepository.findByUserId(userId).ifPresent(reg -> {
            reg.setLastSeen(Instant.now());
            beaconRepository.save(reg);
            log.debug("Heartbeat received: userId={}", userId);
        });
    }

    // ── Peer Discovery ────────────────────────────────────────────

    @Transactional(readOnly = true)
    @Cacheable(value = "activePeers", key = "'all'")
    public BeaconDtos.PeerListResponse getActivePeers() {
        Instant since = Instant.now().minusSeconds(ttlMinutes * 60);
        List<BeaconDtos.BeaconResponse> peers = beaconRepository.findActiveSince(since)
                .stream().map(this::toResponse).collect(Collectors.toList());
        return new BeaconDtos.PeerListResponse(peers.size(), peers);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "activePeers", key = "#teamId")
    public BeaconDtos.PeerListResponse getTeamPeers(UUID teamId) {
        Instant since = Instant.now().minusSeconds(ttlMinutes * 60);
        List<BeaconDtos.BeaconResponse> peers = beaconRepository.findActiveByTeam(teamId, since)
                .stream().map(this::toResponse).collect(Collectors.toList());
        return new BeaconDtos.PeerListResponse(peers.size(), peers);
    }

    // ── Deregistration ────────────────────────────────────────────

    @Transactional
    @CacheEvict(value = "activePeers", allEntries = true)
    public void deregister(UUID userId) {
        beaconRepository.findByUserId(userId).ifPresent(reg -> {
            beaconRepository.delete(reg);
            log.info("Beacon deregistered: userId={}", userId);
        });
    }

    // ── Scheduled TTL Eviction ────────────────────────────────────

    /**
     * Runs every 2 minutes. Deletes any beacon older than TTL.
     * Keeps the active peer table clean when daemons crash without deregistering.
     */
    @Scheduled(fixedRateString = "${beacon.eviction-interval-ms:120000}")
    @CacheEvict(value = "activePeers", allEntries = true)
    @Transactional
    public void evictStaleBeacons() {
        Instant cutoff = Instant.now().minusSeconds(ttlMinutes * 60);
        int deleted = beaconRepository.deleteStaleRegistrations(cutoff);
        if (deleted > 0) {
            log.info("Evicted {} stale beacon registration(s)", deleted);
        }
    }

    private BeaconDtos.BeaconResponse toResponse(BeaconRegistration r) {
        return new BeaconDtos.BeaconResponse(
                r.getId(), r.getUserId(), r.getUsername(),
                r.getLocalIp(), r.getDaemonPort(),
                r.getTeamId(), r.getLastSeen()
        );
    }
}
