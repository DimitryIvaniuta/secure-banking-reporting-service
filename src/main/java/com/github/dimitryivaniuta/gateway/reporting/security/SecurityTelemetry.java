package com.github.dimitryivaniuta.gateway.reporting.security;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

/**
 * Records low-noise security telemetry counters for suspicious traffic handling.
 */
@Component
public class SecurityTelemetry {

    private final Counter suspiciousRequestsCounter;
    private final Counter throttledRequestsCounter;
    private final Counter activeDenyCounter;
    private final AtomicLong lastSuspiciousAtEpochMillis = new AtomicLong();

    /**
     * Creates the telemetry recorder.
     *
     * @param meterRegistry meter registry
     */
    public SecurityTelemetry(MeterRegistry meterRegistry) {
        this.suspiciousRequestsCounter = meterRegistry.counter("security.suspicious.requests");
        this.throttledRequestsCounter = meterRegistry.counter("security.suspicious.requests.throttled");
        this.activeDenyCounter = meterRegistry.counter("security.suspicious.requests.denylist.hits");
    }

    /**
     * Records a suspicious request.
     */
    public void recordSuspiciousRequest() {
        suspiciousRequestsCounter.increment();
        lastSuspiciousAtEpochMillis.set(System.currentTimeMillis());
    }

    /**
     * Records a throttled request.
     */
    public void recordThrottledRequest() {
        throttledRequestsCounter.increment();
        lastSuspiciousAtEpochMillis.set(System.currentTimeMillis());
    }

    /**
     * Records a deny-list hit.
     */
    public void recordDenyHit() {
        activeDenyCounter.increment();
        lastSuspiciousAtEpochMillis.set(System.currentTimeMillis());
    }

    /**
     * Returns the most recent suspicious traffic timestamp.
     *
     * @return epoch milliseconds or 0 when nothing has been recorded yet
     */
    public long getLastSuspiciousAtEpochMillis() {
        return lastSuspiciousAtEpochMillis.get();
    }
}
