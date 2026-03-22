package com.github.dimitryivaniuta.gateway.reporting.dto;

/**
 * JWT token response.
 */
public record AuthTokenResponse(String accessToken, String tokenType, long expiresInSeconds) {
}
