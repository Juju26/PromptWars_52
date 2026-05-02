package com.promptwars.common.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

/**
 * Stateless JWT utility for issuing and validating tokens.
 * Uses HS256 signing with a configurable secret.
 * Designed to be shared across all microservices via common-lib.
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey signingKey;
    private final long accessTokenExpiryMs;
    private final long refreshTokenExpiryMs;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String base64Secret,
            @Value("${jwt.access-token-expiry-ms:900000}") long accessTokenExpiryMs,      // 15 min default
            @Value("${jwt.refresh-token-expiry-ms:604800000}") long refreshTokenExpiryMs  // 7 days default
    ) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Secret));
        this.accessTokenExpiryMs = accessTokenExpiryMs;
        this.refreshTokenExpiryMs = refreshTokenExpiryMs;
    }

    /**
     * Generates a signed access JWT for the given subject (userId) and extra claims.
     */
    public String generateAccessToken(String subject, Map<String, Object> extraClaims) {
        return buildToken(subject, extraClaims, accessTokenExpiryMs);
    }

    /**
     * Generates a signed refresh JWT (minimal claims).
     */
    public String generateRefreshToken(String subject) {
        return buildToken(subject, Map.of("type", "refresh"), refreshTokenExpiryMs);
    }

    private String buildToken(String subject, Map<String, Object> claims, long expiryMs) {
        Date now = new Date();
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiryMs))
                .signWith(signingKey)
                .compact();
    }

    /**
     * Validates the token signature and expiry. Returns false instead of throwing.
     */
    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            log.warn("JWT validation failed: {}", ex.getMessage());
            return false;
        }
    }

    public String extractSubject(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(parseClaims(token));
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
