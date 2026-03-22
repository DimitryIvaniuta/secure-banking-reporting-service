package com.github.dimitryivaniuta.gateway.reporting.controller;

import com.github.dimitryivaniuta.gateway.reporting.dto.AuthTokenRequest;
import com.github.dimitryivaniuta.gateway.reporting.dto.AuthTokenResponse;
import com.github.dimitryivaniuta.gateway.reporting.security.JwtService;
import com.github.dimitryivaniuta.gateway.reporting.service.LocalUserAuthenticationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Issues local JWT tokens for Postman and demo usage.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final LocalUserAuthenticationService authenticationService;
    private final JwtService jwtService;

    /**
     * Creates the controller.
     *
     * @param authenticationService local auth service
     * @param jwtService JWT service
     */
    public AuthController(LocalUserAuthenticationService authenticationService, JwtService jwtService) {
        this.authenticationService = authenticationService;
        this.jwtService = jwtService;
    }

    /**
     * Issues a bearer token for valid local credentials.
     *
     * @param request login request
     * @return token response or 401
     */
    @PostMapping("/token")
    public ResponseEntity<AuthTokenResponse> createToken(@Valid @RequestBody AuthTokenRequest request) {
        if (!authenticationService.isValid(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<String> roles = request.username().contains("admin") ? List.of("ADMIN", "VIEWER") : List.of("VIEWER");
        return ResponseEntity.ok(new AuthTokenResponse(
                jwtService.issueToken(request.username(), roles),
                "Bearer",
                jwtService.expirationSeconds()
        ));
    }
}
