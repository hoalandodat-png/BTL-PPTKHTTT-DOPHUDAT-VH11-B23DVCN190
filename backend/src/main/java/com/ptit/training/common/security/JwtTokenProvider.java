package com.ptit.training.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.HexFormat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Sinh và xác thực JWT Access Token, đồng thời hỗ trợ tạo/băm Refresh Token và Reset Token.
 */
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long accessExpirationMs;
    private final long refreshExpirationMs;
    private final SecureRandom secureRandom = new SecureRandom();

    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-token-expiration-ms:900000}") long accessExpirationMs,
            @Value("${app.jwt.refresh-token-expiration-ms:604800000}") long refreshExpirationMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpirationMs = accessExpirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    public String generateAccessToken(UserPrincipal principal) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessExpirationMs);
        return Jwts.builder()
                .subject(String.valueOf(principal.getId()))
                .claim("username", principal.getUsername())
                .claim("email", principal.getEmail())
                .claim("roles", principal.getRoles())
                .claim("permissions", principal.getPermissions())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    public UserPrincipal parseAccessToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Long userId = Long.parseLong(claims.getSubject());
        String username = claims.get("username", String.class);
        String email = claims.get("email", String.class);
        Set<String> roles = toStringSet(claims.get("roles", Collection.class));
        Set<String> permissions = toStringSet(claims.get("permissions", Collection.class));

        return new UserPrincipal(userId, username, email, "", true, roles, permissions);
    }

    /**
     * Token đặt lại mật khẩu có hạn 15 phút, gắn fingerprint của passwordHash hiện tại để tự vô hiệu sau 1 lần dùng.
     */
    public String generatePasswordResetToken(Long userId, String passwordHashPrefix) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + 900_000L);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("type", "PASSWORD_RESET")
                .claim("fp", passwordHashPrefix)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    public Claims parsePasswordResetToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        if (!"PASSWORD_RESET".equals(claims.get("type", String.class))) {
            throw new IllegalArgumentException("Invalid token type");
        }
        return claims;
    }

    public String generateOpaqueRefreshToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public String sha256Hex(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    public long getAccessExpirationMs() { return accessExpirationMs; }
    public long getRefreshExpirationMs() { return refreshExpirationMs; }

    private Set<String> toStringSet(Collection<?> raw) {
        if (raw == null) return Set.of();
        Set<String> result = new LinkedHashSet<>();
        for (Object item : raw) {
            if (item != null) result.add(item.toString());
        }
        return result;
    }
}
