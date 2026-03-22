package com.github.dimitryivaniuta.gateway.reporting.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Browser bootstrap endpoints for portal security.
 */
@RestController
@RequestMapping("/portal/security")
public class PortalSecurityController {

    /**
     * Returns the CSRF token to browser clients. Spring also writes it into the cookie repository.
     *
     * @param request servlet request containing the token attribute
     * @return token body
     */
    @GetMapping("/csrf")
    public Map<String, String> csrf(HttpServletRequest request) {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        return Map.of(
                "headerName", csrfToken.getHeaderName(),
                "parameterName", csrfToken.getParameterName(),
                "token", csrfToken.getToken()
        );
    }
}
