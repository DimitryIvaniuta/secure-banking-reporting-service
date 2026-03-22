package com.github.dimitryivaniuta.gateway.reporting.security;

import com.github.dimitryivaniuta.gateway.reporting.config.SecurityProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Validates Origin or Referer on unsafe browser-targeted requests.
 */
@Component
public class BrowserOriginValidationFilter extends OncePerRequestFilter {

    private static final Set<String> UNSAFE_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");

    private final SecurityProperties securityProperties;

    /**
     * Creates the filter.
     *
     * @param securityProperties typed security properties
     */
    public BrowserOriginValidationFilter(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/portal/")
                || !UNSAFE_METHODS.contains(request.getMethod());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String origin = request.getHeader("Origin");
        String referer = request.getHeader("Referer");

        if (isAllowed(origin) || isAllowed(referer)) {
            filterChain.doFilter(request, response);
            return;
        }

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.getWriter().write("""
                {"type":"about:blank","title":"Forbidden","status":403,
                "detail":"Unsafe browser request rejected due to missing or untrusted Origin/Referer."}
                """);
    }

    private boolean isAllowed(String source) {
        if (source == null || source.isBlank()) {
            return false;
        }
        try {
            URI uri = URI.create(source);
            String normalizedOrigin = uri.getScheme() + "://" + uri.getHost()
                    + (uri.getPort() > 0 ? ":" + uri.getPort() : "");
            return securityProperties.getBrowser().getAllowedOrigins().stream()
                    .anyMatch(allowed -> allowed.equalsIgnoreCase(normalizedOrigin));
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}
