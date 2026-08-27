package com.hunghv.inmobitestjava.service;

import com.hunghv.inmobitestjava.security.JwtProperties;
import com.hunghv.inmobitestjava.security.UserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties properties;

    private SecretKey signingKey;

    @PostConstruct
    void init() {
        this.signingKey = createSigningKey(properties.getSecret());
    }

    public String generateToken(UserPrincipal principal) {
        Instant now = Instant.now();
        return Jwts.builder()
            .claims(Map.of("userId", principal.getId(), "email", principal.getUsername()))
            .subject(principal.getUsername())
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusMillis(properties.getExpirationMs())))
            .signWith(signingKey)
            .compact();
    }

    public String extractEmail(String token) {
        return claims(token).getSubject();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            Claims c = claims(token);
            return userDetails.getUsername().equals(c.getSubject())
                && !c.getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("Token validation failed: {}", ex.getMessage());
            return false;
        }
    }

    private Claims claims(String token) {
        return Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    private static SecretKey createSigningKey(String secret) {
        try {
            return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        } catch (Exception ex) {
            return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        }
    }
}
