package com.promptwars.messaging.controller;

import com.promptwars.common.dto.ApiResponse;
import com.promptwars.messaging.dto.MessagingDtos;
import com.promptwars.messaging.service.MessagingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/messaging")
@RequiredArgsConstructor
@Tag(name = "Messaging", description = "Channels and messages (Slack-like)")
@SecurityRequirement(name = "bearerAuth")
public class MessagingController {

    private final MessagingService messagingService;

    // ── Channels ──────────────────────────────────────────────────

    @PostMapping("/channels")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new channel")
    public ApiResponse<MessagingDtos.ChannelResponse> createChannel(
            @Valid @RequestBody MessagingDtos.CreateChannelRequest request,
            @AuthenticationPrincipal String userId
    ) {
        return ApiResponse.ok("Channel created",
                messagingService.createChannel(request, UUID.fromString(userId)));
    }

    @GetMapping("/channels/team/{teamId}")
    @Operation(summary = "List all channels for a team")
    public ApiResponse<List<MessagingDtos.ChannelResponse>> getTeamChannels(@PathVariable UUID teamId) {
        return ApiResponse.ok(messagingService.getTeamChannels(teamId));
    }

    @GetMapping("/channels/public")
    @Operation(summary = "List all public (GENERAL) channels")
    public ApiResponse<List<MessagingDtos.ChannelResponse>> getPublicChannels() {
        return ApiResponse.ok(messagingService.getPublicChannels());
    }

    // ── Messages ──────────────────────────────────────────────────

    @PostMapping("/channels/{channelId}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Send a message to a channel")
    public ApiResponse<MessagingDtos.MessageResponse> sendMessage(
            @PathVariable UUID channelId,
            @Valid @RequestBody MessagingDtos.SendMessageRequest request,
            @AuthenticationPrincipal String userId
    ) {
        String username = extractUsername();
        return ApiResponse.ok("Message sent",
                messagingService.sendMessage(channelId, request, UUID.fromString(userId), username));
    }

    @GetMapping("/channels/{channelId}/messages")
    @Operation(summary = "Get messages (cursor-based, newest-first). Use 'before' ISO timestamp to paginate.")
    public ApiResponse<List<MessagingDtos.MessageResponse>> getMessages(
            @PathVariable UUID channelId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant before,
            @RequestParam(defaultValue = "50") int limit
    ) {
        int safeLimit = Math.min(limit, 100);
        return ApiResponse.ok(messagingService.getMessages(channelId, before, safeLimit));
    }

    @DeleteMapping("/messages/{messageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Soft-delete a message (own messages only)")
    public void deleteMessage(
            @PathVariable UUID messageId,
            @AuthenticationPrincipal String userId
    ) {
        messagingService.deleteMessage(messageId, UUID.fromString(userId));
    }

    // ── Announcements (Organiser only) ────────────────────────────

    @PostMapping("/announcements")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    @Operation(summary = "Broadcast an announcement via Pub/Sub (organiser only)")
    public ApiResponse<Void> publishAnnouncement(
            @Valid @RequestBody MessagingDtos.AnnouncementRequest request
    ) {
        messagingService.publishAnnouncement(request);
        return ApiResponse.ok("Announcement published");
    }

    private String extractUsername() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getDetails() instanceof String detail) return detail;
        return "unknown";
    }
}
