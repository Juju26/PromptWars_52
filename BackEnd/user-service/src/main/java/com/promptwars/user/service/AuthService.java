package com.promptwars.user.service;

import com.promptwars.common.exception.BusinessException;
import com.promptwars.common.security.JwtTokenProvider;
import com.promptwars.user.domain.User;
import com.promptwars.user.dto.AuthRequest;
import com.promptwars.user.dto.AuthResponse;
import com.promptwars.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Core auth service — registration, login, token refresh.
 * All operations are transactional; passwords are bcrypt-hashed.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${jwt.access-token-expiry-ms:900000}")
    private long accessTokenExpiryMs;

    @Transactional
    public AuthResponse register(AuthRequest.Register request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email already registered: " + request.email());
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new BusinessException("Username already taken: " + request.username());
        }

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .displayName(request.displayName() != null ? request.displayName() : request.username())
                .role(User.Role.PARTICIPANT)
                .build();

        user = userRepository.save(user);
        log.info("New user registered: id={}, email={}", user.getId(), user.getEmail());

        return buildAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(AuthRequest.Login request) {
        User user = userRepository.findActiveByEmail(request.email())
                .orElseThrow(() -> new BusinessException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException("Invalid email or password");
        }

        log.info("User logged in: id={}", user.getId());
        return buildAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.isTokenValid(refreshToken)) {
            throw new BusinessException("Invalid or expired refresh token");
        }

        String userId = jwtTokenProvider.extractSubject(refreshToken);
        String type = jwtTokenProvider.extractClaim(refreshToken,
                claims -> (String) claims.get("type"));

        if (!"refresh".equals(type)) {
            throw new BusinessException("Not a refresh token");
        }

        User user = userRepository.findById(java.util.UUID.fromString(userId))
                .orElseThrow(() -> new BusinessException("User not found"));

        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        Map<String, Object> claims = Map.of(
                "role", "ROLE_" + user.getRole().name(),
                "email", user.getEmail(),
                "username", user.getUsername()
        );

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId().toString(), claims);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId().toString());

        return new AuthResponse(
                accessToken,
                refreshToken,
                accessTokenExpiryMs / 1000,
                new AuthResponse.UserProfile(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getDisplayName(),
                        user.getRole(),
                        user.getTeamId(),
                        user.getCreatedAt()
                )
        );
    }
}
