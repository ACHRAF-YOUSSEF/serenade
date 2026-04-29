package com.serenade.backend.domain.auth;

import com.serenade.backend.config.AppProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private static final String CLAIM_TYPE = "type";
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";

    private final AppProperties props;
    private SecretKey signingKey;

    public JwtService(AppProperties props) {
        this.props = props;
    }

    @PostConstruct
    void init() {
        if (props.jwt().secret() == null || props.jwt().secret().isBlank()) {
            throw new IllegalStateException("JWT_SECRET must be set");
        }
        signingKey = Keys.hmacShaKeyFor(props.jwt().secret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(UUID userId) {
        return buildToken(userId.toString(), TYPE_ACCESS, props.jwt().accessTokenExpiryMs());
    }

    public String generateRefreshToken(UUID userId) {
        return buildToken(userId.toString(), TYPE_REFRESH, props.jwt().refreshTokenExpiryMs());
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(parseClaims(token).getSubject());
    }

    public boolean isAccessToken(String token) {
        return TYPE_ACCESS.equals(parseClaims(token).get(CLAIM_TYPE, String.class));
    }

    public boolean isRefreshToken(String token) {
        return TYPE_REFRESH.equals(parseClaims(token).get(CLAIM_TYPE, String.class));
    }

    private String buildToken(String subject, String type, long expiryMs) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(subject)
                .claim(CLAIM_TYPE, type)
                .issuedAt(new Date(now))
                .expiration(new Date(now + expiryMs))
                // verifyWith enforces a real algorithm — alg:none is rejected unconditionally
                .signWith(signingKey)
                .compact();
    }

    private Claims parseClaims(String token) {
        // verifyWith rejects unsigned (alg:none) tokens — security non-negotiable
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
