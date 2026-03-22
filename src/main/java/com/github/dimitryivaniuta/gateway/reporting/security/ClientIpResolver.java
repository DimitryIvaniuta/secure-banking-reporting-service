package com.github.dimitryivaniuta.gateway.reporting.security;

import com.github.dimitryivaniuta.gateway.reporting.config.SecurityProperties;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Resolves the client IP from trusted forwarding headers or remote address.
 */
@Component
public class ClientIpResolver {

    private final List<String> trustedForwarderAddresses;

    /**
     * Creates the resolver.
     *
     * @param securityProperties typed security properties
     */
    public ClientIpResolver(SecurityProperties securityProperties) {
        this.trustedForwarderAddresses = securityProperties.getProxy().getTrustedForwarderAddresses();
    }

    /**
     * Resolves the client IP address.
     *
     * @param request servlet request
     * @return resolved client IP
     */
    public String resolve(HttpServletRequest request) {
        String remoteAddress = request.getRemoteAddr();
        if (!trustedForwarderAddresses.contains(remoteAddress)) {
            return remoteAddress;
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return remoteAddress;
    }
}
