package com.promptwars.beacon.dto;

import jakarta.validation.constraints.*;

import java.time.Instant;
import java.util.UUID;

public final class BeaconDtos {

    private BeaconDtos() {}

    public record RegisterRequest(
            @NotBlank(message = "localIp is required")
            @Pattern(
                regexp = "^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)"
                       + "|([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$",
                message = "Must be a valid IPv4 or IPv6 address"
            )
            String localIp,

            @Min(value = 1024, message = "Port must be >= 1024")
            @Max(value = 65535, message = "Port must be <= 65535")
            int daemonPort,

            UUID teamId
    ) {}

    public record BeaconResponse(
            UUID id,
            UUID userId,
            String username,
            String localIp,
            int daemonPort,
            UUID teamId,
            Instant lastSeen
    ) {}

    public record PeerListResponse(
            int totalActive,
            java.util.List<BeaconResponse> peers
    ) {}
}
