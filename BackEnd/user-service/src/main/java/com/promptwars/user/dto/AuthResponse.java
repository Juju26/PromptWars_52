package com.promptwars.user.dto;

import com.promptwars.user.domain.User;

import java.time.Instant;
import java.util.UUID;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        long expiresInSeconds,
        UserProfile user
) {
    public record UserProfile(
            UUID id,
            String username,
            String email,
            String displayName,
            User.Role role,
            UUID teamId,
            Instant createdAt
    ) {}
}
