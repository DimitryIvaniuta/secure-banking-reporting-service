package com.github.dimitryivaniuta.gateway.reporting.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Strongly typed application security properties.
 */
@ConfigurationProperties(prefix = "app.security")
public class SecurityProperties {

    private final Jwt jwt = new Jwt();
    private final Cors cors = new Cors();
    private final Throttle throttle = new Throttle();
    private final Browser browser = new Browser();
    private final Proxy proxy = new Proxy();

    public Jwt getJwt() {
        return jwt;
    }

    public Cors getCors() {
        return cors;
    }

    public Throttle getThrottle() {
        return throttle;
    }

    public Browser getBrowser() {
        return browser;
    }

    public Proxy getProxy() {
        return proxy;
    }

    /**
     * JWT properties.
     */
    public static class Jwt {
        private String secretBase64;
        private long expirationSeconds = 3600;

        public String getSecretBase64() {
            return secretBase64;
        }

        public void setSecretBase64(String secretBase64) {
            this.secretBase64 = secretBase64;
        }

        public long getExpirationSeconds() {
            return expirationSeconds;
        }

        public void setExpirationSeconds(long expirationSeconds) {
            this.expirationSeconds = expirationSeconds;
        }
    }

    /**
     * CORS properties.
     */
    public static class Cors {
        private List<String> allowedOrigins = List.of("http://localhost:3000");

        public List<String> getAllowedOrigins() {
            return allowedOrigins;
        }

        public void setAllowedOrigins(List<String> allowedOrigins) {
            this.allowedOrigins = allowedOrigins;
        }
    }

    /**
     * Request throttling properties.
     */
    public static class Throttle {
        private long windowSeconds = 60;
        private long maxRequestsPerWindow = 30;
        private long denySeconds = 900;
        private List<String> trustedApplicationPrefixes = List.of(
                "/api/",
                "/auth/",
                "/portal/",
                "/actuator/",
                "/error"
        );

        public long getWindowSeconds() {
            return windowSeconds;
        }

        public void setWindowSeconds(long windowSeconds) {
            this.windowSeconds = windowSeconds;
        }

        public long getMaxRequestsPerWindow() {
            return maxRequestsPerWindow;
        }

        public void setMaxRequestsPerWindow(long maxRequestsPerWindow) {
            this.maxRequestsPerWindow = maxRequestsPerWindow;
        }

        public long getDenySeconds() {
            return denySeconds;
        }

        public void setDenySeconds(long denySeconds) {
            this.denySeconds = denySeconds;
        }

        public List<String> getTrustedApplicationPrefixes() {
            return trustedApplicationPrefixes;
        }

        public void setTrustedApplicationPrefixes(List<String> trustedApplicationPrefixes) {
            this.trustedApplicationPrefixes = trustedApplicationPrefixes;
        }
    }

    /**
     * Browser request protection properties.
     */
    public static class Browser {
        private List<String> allowedOrigins = List.of("http://localhost:8080", "http://localhost:3000");

        public List<String> getAllowedOrigins() {
            return allowedOrigins;
        }

        public void setAllowedOrigins(List<String> allowedOrigins) {
            this.allowedOrigins = allowedOrigins;
        }
    }

    /**
     * Trusted proxy properties.
     */
    public static class Proxy {
        private List<String> trustedForwarderAddresses = List.of("127.0.0.1", "::1");

        public List<String> getTrustedForwarderAddresses() {
            return trustedForwarderAddresses;
        }

        public void setTrustedForwarderAddresses(List<String> trustedForwarderAddresses) {
            this.trustedForwarderAddresses = trustedForwarderAddresses;
        }
    }
}
