package com.github.dimitryivaniuta.gateway.reporting.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Early request filter that throttles obvious internet noise before business processing.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SuspiciousTrafficFilter extends OncePerRequestFilter {

    private final SuspiciousRequestService suspiciousRequestService;
    private final ClientIpResolver clientIpResolver;
    private final SecurityTelemetry securityTelemetry;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        String method = request.getMethod();
        String clientIp = clientIpResolver.resolve(request);

        if (suspiciousRequestService.isTemporarilyDenied(clientIp)) {
            securityTelemetry.recordDenyHit();
            writeTooManyRequests(response);
            return;
        }

        if (suspiciousRequestService.isSuspicious(method, path)) {
            boolean throttled = suspiciousRequestService.registerAndShouldThrottle(clientIp);
            securityTelemetry.recordSuspiciousRequest();
            log.warn("Suspicious request detected: method={}, path={}, clientIp={}, throttled={}", method, path, clientIp, throttled);
            if (throttled) {
                securityTelemetry.recordThrottledRequest();
                writeTooManyRequests(response);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void writeTooManyRequests(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_TOO_MANY_REQUESTS);
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.getWriter().write("""
                {"type":"about:blank","title":"Too Many Requests","status":429,
                "detail":"Suspicious request rate exceeded the temporary threshold."}
                """);
    }
}
