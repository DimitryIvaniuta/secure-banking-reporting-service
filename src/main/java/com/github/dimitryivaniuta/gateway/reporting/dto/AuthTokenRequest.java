package com.github.dimitryivaniuta.gateway.reporting.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Simple demo authentication request for local development.
 */
public record AuthTokenRequest(@NotBlank String username, @NotBlank String password) {
}
