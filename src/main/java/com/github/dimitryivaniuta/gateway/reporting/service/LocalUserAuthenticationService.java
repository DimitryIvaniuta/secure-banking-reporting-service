package com.github.dimitryivaniuta.gateway.reporting.service;

import com.github.dimitryivaniuta.gateway.reporting.dto.AuthTokenRequest;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Very small local authentication service for demo and Postman usage.
 */
@Service
public class LocalUserAuthenticationService {

    private static final Map<String, String> USERS = Map.of(
            "report-admin", "changeit-admin",
            "report-viewer", "changeit-viewer"
    );

    /**
     * Validates static local credentials.
     *
     * @param request login request
     * @return true when credentials are valid
     */
    public boolean isValid(AuthTokenRequest request) {
        return request.password().equals(USERS.get(request.username()));
    }
}
