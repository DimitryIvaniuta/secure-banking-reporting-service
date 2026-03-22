package com.github.dimitryivaniuta.gateway.reporting.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Issues and validates HMAC-signed JWT tokens for local demo usage.
 */
@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long expirationSeconds;

    /**
     * Creates the JWT service.
     *
     * @param secret base64-encoded HMAC secret
     * @param expirationSeconds token lifetime in seconds
     */
    public JwtService(
            @Value("${app.security.jwt.secret-base64}") String secret,
            @Value("${app.security.jwt.expiration-seconds}") long expirationSeconds
    ) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expirationSeconds = expirationSeconds;
    }

    /**
     * Creates a new bearer token.
     *
     * @param subject username
     * @param roles role names
     * @return compact JWT token
     */
    public String issueToken(String subject, List<String> roles) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(subject)
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expirationSeconds)))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Parses claims from a token.
     *
     * @param token JWT token
     * @return claims
     */
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Returns configured lifetime.
     *
     * @return expiration seconds
     */
    public long expirationSeconds() {
        return expirationSeconds;
    }
}
