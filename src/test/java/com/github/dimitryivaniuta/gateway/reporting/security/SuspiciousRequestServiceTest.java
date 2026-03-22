package com.github.dimitryivaniuta.gateway.reporting.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.dimitryivaniuta.gateway.reporting.config.SecurityProperties;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

/**
 * Tests suspicious request classification and throttling behavior.
 */
@ExtendWith(MockitoExtension.class)
class SuspiciousRequestServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private SuspiciousRequestService service;

    @BeforeEach
    void setUp() {
        SecurityProperties properties = new SecurityProperties();
        properties.getThrottle().setWindowSeconds(60L);
        properties.getThrottle().setMaxRequestsPerWindow(2L);
        properties.getThrottle().setDenySeconds(120L);
        properties.getThrottle().setTrustedApplicationPrefixes(List.of("/api/", "/auth/", "/portal/", "/actuator/", "/error"));
        service = new SuspiciousRequestService(redisTemplate, properties);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void shouldDetectKnownSuspiciousPath() {
        assertThat(service.isSuspicious("POST", "/form/v2/upload")).isTrue();
        assertThat(service.isSuspicious("GET", "/form/v2/upload")).isFalse();
        assertThat(service.isSuspicious("POST", "/api/reports")).isFalse();
    }

    @Test
    void shouldDetectUnknownUnsafePathAsSuspicious() {
        assertThat(service.isSuspicious("POST", "/totally-random-probe")).isTrue();
    }

    @Test
    void shouldThrottleAfterConfiguredLimit() {
        when(valueOperations.increment("security:ip:1.2.3.4")).thenReturn(1L, 2L, 3L);

        assertThat(service.registerAndShouldThrottle("1.2.3.4")).isFalse();
        assertThat(service.registerAndShouldThrottle("1.2.3.4")).isFalse();
        assertThat(service.registerAndShouldThrottle("1.2.3.4")).isTrue();
        verify(valueOperations).set("security:deny:1.2.3.4", "1", java.time.Duration.ofSeconds(120L));
    }

    @Test
    void shouldReturnFalseForDenyMarkerWhenRedisReportsNoKey() {
        when(redisTemplate.hasKey("security:deny:1.2.3.4")).thenReturn(false);

        assertThat(service.isTemporarilyDenied("1.2.3.4")).isFalse();
        verify(redisTemplate, never()).expire("security:deny:1.2.3.4", java.time.Duration.ofSeconds(120L));
    }
}
