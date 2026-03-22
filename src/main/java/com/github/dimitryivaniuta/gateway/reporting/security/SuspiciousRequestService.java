package com.github.dimitryivaniuta.gateway.reporting.security;

import com.github.dimitryivaniuta.gateway.reporting.config.SecurityProperties;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Tracks and throttles suspicious or abusive traffic.
 */
@Service
public class SuspiciousRequestService {

    private static final Set<String> WRITE_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");

    private final StringRedisTemplate redisTemplate;
    private final SecurityProperties securityProperties;
    private final Set<String> localFallback = ConcurrentHashMap.newKeySet();

    /**
     * Creates the service.
     *
     * @param redisTemplate redis client
     * @param securityProperties typed security properties
     */
    public SuspiciousRequestService(StringRedisTemplate redisTemplate, SecurityProperties securityProperties) {
        this.redisTemplate = redisTemplate;
        this.securityProperties = securityProperties;
    }

    /**
     * Determines whether the request looks suspicious and should count toward throttling.
     *
     * @param method HTTP method
     * @param path request path
     * @return true when suspicious
     */
    public boolean isSuspicious(String method, String path) {
        if (!WRITE_METHODS.contains(method)) {
            return false;
        }
        if (!isKnownApplicationPath(path)) {
            return true;
        }
        String lower = path.toLowerCase();
        return lower.startsWith("/form/")
                || lower.startsWith("/wp-")
                || lower.startsWith("/php")
                || lower.startsWith("/.env")
                || lower.startsWith("/cgi-bin")
                || lower.contains("upload")
                || lower.contains("admin")
                || lower.contains("prospect")
                || lower.contains("wordpress")
                || lower.contains("xmlrpc");
    }

    /**
     * Returns whether the IP is already in temporary deny state.
     *
     * @param clientIp client IP address
     * @return true when deny marker exists
     */
    public boolean isTemporarilyDenied(String clientIp) {
        String key = denyKey(clientIp);
        try {
            Boolean present = redisTemplate.hasKey(key);
            return Boolean.TRUE.equals(present);
        } catch (RuntimeException exception) {
            return localFallback.contains(key);
        }
    }

    /**
     * Registers a suspicious request and returns whether the caller should be throttled.
     *
     * @param clientIp client IP address
     * @return true when limit exceeded
     */
    public boolean registerAndShouldThrottle(String clientIp) {
        String key = countKey(clientIp);
        long windowSeconds = securityProperties.getThrottle().getWindowSeconds();
        long maxRequestsPerWindow = securityProperties.getThrottle().getMaxRequestsPerWindow();
        long denySeconds = securityProperties.getThrottle().getDenySeconds();
        try {
            Long count = redisTemplate.opsForValue().increment(key);
            if (count != null && count == 1L) {
                redisTemplate.expire(key, Duration.ofSeconds(windowSeconds));
            }
            if (count != null && count > maxRequestsPerWindow) {
                redisTemplate.opsForValue().set(denyKey(clientIp), "1", Duration.ofSeconds(denySeconds));
                return true;
            }
            return false;
        } catch (RuntimeException exception) {
            localFallback.add(key);
            return false;
        }
    }

    /**
     * Checks whether a local in-memory fallback marker exists.
     *
     * @param clientIp client IP address
     * @return true when remembered locally
     */
    public boolean isKnownLocally(String clientIp) {
        return localFallback.contains(countKey(clientIp)) || localFallback.contains(denyKey(clientIp));
    }

    private boolean isKnownApplicationPath(String path) {
        List<String> prefixes = securityProperties.getThrottle().getTrustedApplicationPrefixes();
        return prefixes.stream().anyMatch(path::startsWith);
    }

    private String countKey(String clientIp) {
        return "security:ip:" + clientIp;
    }

    private String denyKey(String clientIp) {
        return "security:deny:" + clientIp;
    }
}
