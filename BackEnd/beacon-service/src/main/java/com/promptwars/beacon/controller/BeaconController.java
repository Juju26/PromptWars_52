package com.promptwars.beacon.controller;

import com.promptwars.common.dto.ApiResponse;
import com.promptwars.beacon.dto.BeaconDtos;
import com.promptwars.beacon.service.BeaconService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/beacon")
@RequiredArgsConstructor
@Tag(name = "Beacon Registry", description = "P2P peer discovery — LAN IP registration and lookup")
@SecurityRequirement(name = "bearerAuth")
public class BeaconController {

    private final BeaconService beaconService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Register or refresh this daemon's LAN IP in the beacon registry",
        description = "Call on startup and periodically as a heartbeat. Replaces any prior registration for this user."
    )
    public ApiResponse<BeaconDtos.BeaconResponse> register(
            @Valid @RequestBody BeaconDtos.RegisterRequest request,
            @AuthenticationPrincipal String userId
    ) {
        String username = extractUsername();
        return ApiResponse.ok("Beacon registered",
                beaconService.register(request, UUID.fromString(userId), username));
    }

    @PostMapping("/heartbeat")
    @Operation(summary = "Refresh lastSeen timestamp without changing IP (lightweight keepalive)")
    public ApiResponse<Void> heartbeat(@AuthenticationPrincipal String userId) {
        beaconService.heartbeat(UUID.fromString(userId));
        return ApiResponse.ok("Heartbeat acknowledged");
    }

    @GetMapping("/peers")
    @Operation(
        summary = "Get all currently active peers",
        description = "Returns peers seen within the configured TTL window (default 10 min). Cached to handle burst discovery."
    )
    public ApiResponse<BeaconDtos.PeerListResponse> getActivePeers() {
        return ApiResponse.ok(beaconService.getActivePeers());
    }

    @GetMapping("/peers/team/{teamId}")
    @Operation(summary = "Get active peers belonging to a specific team")
    public ApiResponse<BeaconDtos.PeerListResponse> getTeamPeers(@PathVariable UUID teamId) {
        return ApiResponse.ok(beaconService.getTeamPeers(teamId));
    }

    @DeleteMapping("/deregister")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Explicitly deregister this daemon from the beacon registry on graceful shutdown")
    public void deregister(@AuthenticationPrincipal String userId) {
        beaconService.deregister(UUID.fromString(userId));
    }

    private String extractUsername() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? (String) auth.getPrincipal() : "unknown";
    }
}
