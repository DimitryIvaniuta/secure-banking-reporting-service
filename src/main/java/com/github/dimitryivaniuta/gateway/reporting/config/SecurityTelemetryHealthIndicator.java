package com.github.dimitryivaniuta.gateway.reporting.config;

import com.github.dimitryivaniuta.gateway.reporting.security.SecurityTelemetry;
import com.github.dimitryivaniuta.gateway.reporting.security.SuspiciousRequestService;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Lightweight health indicator exposing suspicious-traffic protection status.
 */
@Component
public class SecurityTelemetryHealthIndicator implements HealthIndicator {

    private final SuspiciousRequestService suspiciousRequestService;
    private final SecurityTelemetry securityTelemetry;

    /**
     * Creates the indicator.
     *
     * @param suspiciousRequestService suspicious request service
     * @param securityTelemetry telemetry recorder
     */
    public SecurityTelemetryHealthIndicator(
            SuspiciousRequestService suspiciousRequestService,
            SecurityTelemetry securityTelemetry
    ) {
        this.suspiciousRequestService = suspiciousRequestService;
        this.securityTelemetry = securityTelemetry;
    }

    /**
     * Reports security telemetry subsystem health.
     *
     * @return health payload
     */
    @Override
    public Health health() {
        return Health.up()
                .withDetail("suspiciousTrafficProtection", "enabled")
                .withDetail("redisFallbackMarkersObserved", suspiciousRequestService.isKnownLocally("127.0.0.1"))
                .withDetail("lastSuspiciousTrafficAtEpochMillis", securityTelemetry.getLastSuspiciousAtEpochMillis())
                .build();
    }
}
