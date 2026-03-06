package com.ai.interview.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class TokenService {

    @Value("${security.jwt.secret}")
    private String jwtSecret;

    @Value("${security.jwt.access-token-expire-minutes:240}")
    private long accessTokenExpireMinutes;

    public String generateToken(AppUserPrincipal principal) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(principal.getUsername())
                .claim("uid", principal.getId())
                .claim("role", principal.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(accessTokenExpireMinutes, ChronoUnit.MINUTES)))
                .signWith(signingKey())
                .compact();
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public AuthenticatedUser parseToken(String token) {
        Claims claims = parseClaims(token);
        Long userId = claims.get("uid", Long.class);
        String username = claims.getSubject();
        String role = claims.get("role", String.class);

        if (userId == null || username == null || username.isBlank() || role == null || role.isBlank()) {
            throw new IllegalArgumentException("登录凭证无效");
        }

        return new AuthenticatedUser(userId, username, role);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        AuthenticatedUser authenticatedUser = parseToken(token);
        if (!userDetails.getUsername().equals(authenticatedUser.username())) {
            return false;
        }

        if (userDetails instanceof AppUserPrincipal principal) {
            return principal.getRole().name().equals(authenticatedUser.role());
        }

        return true;
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(signingKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException ex) {
            throw new IllegalArgumentException("登录凭证无效", ex);
        }
    }

    private SecretKey signingKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException("security.jwt.secret 至少需要 32 个字节");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}