package com.promptwars.messaging.dto;

import com.promptwars.messaging.domain.Channel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public final class MessagingDtos {

    private MessagingDtos() {}

    public record CreateChannelRequest(
            @NotBlank @Size(max = 100) String name,
            String description,
            Channel.ChannelType type,
            UUID teamId
    ) {}

    public record SendMessageRequest(
            @NotBlank(message = "Message content cannot be empty")
            @Size(max = 4000, message = "Message too long (max 4000 chars)")
            String content
    ) {}

    public record AnnouncementRequest(
            @NotBlank String title,
            @NotBlank @Size(max = 2000) String body
    ) {}

    public record ChannelResponse(
            UUID id,
            String name,
            String description,
            Channel.ChannelType type,
            UUID teamId,
            Instant createdAt
    ) {}

    public record MessageResponse(
            UUID id,
            UUID channelId,
            UUID senderId,
            String senderUsername,
            String content,
            Instant createdAt
    ) {}

    public record CursorPage(
            @NotNull Instant before,
            int limit
    ) {}
}
