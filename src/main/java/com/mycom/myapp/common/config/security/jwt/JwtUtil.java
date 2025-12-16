package com.mycom.myapp.common.config.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey key;

    @Value("${jwt.issuer}") private String issuer;
    @Value("${jwt.access_expiration_time}") private long accessTokenExpirationTime;
    @Value("${jwt.refresh_expiration_time}") private long refreshExpirationTime;

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(Long userId, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer(issuer)
                .subject(String.valueOf(userId))
                .claim("role", role)
                .claim("typ", "access")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessTokenExpirationTime)))
                .signWith(key)
                .compact();
    }

    public String createRefreshToken(Long userId, String jti) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer(issuer)
                .subject(String.valueOf(userId))
                .claim("typ", "refresh")
                .id(jti)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(refreshExpirationTime)))
                .signWith(key)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isAccess(Claims c)  { return "access".equals(c.get("typ")); }
    public boolean isRefresh(Claims c) { return "refresh".equals(c.get("typ")); }

    public long getAccessTtlSeconds() { return accessTokenExpirationTime; }
    public long getRefreshTtlSeconds() { return refreshExpirationTime; }
}
