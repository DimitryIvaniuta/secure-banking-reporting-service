package com.github.dimitryivaniuta.gateway.reporting.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.dimitryivaniuta.gateway.reporting.config.SecurityConfig;
import com.github.dimitryivaniuta.gateway.reporting.security.BrowserOriginValidationFilter;
import com.github.dimitryivaniuta.gateway.reporting.security.ClientIpResolver;
import com.github.dimitryivaniuta.gateway.reporting.security.JwtService;
import com.github.dimitryivaniuta.gateway.reporting.security.SecurityTelemetry;
import com.github.dimitryivaniuta.gateway.reporting.security.SuspiciousRequestService;
import com.github.dimitryivaniuta.gateway.reporting.security.SuspiciousTrafficFilter;
import com.github.dimitryivaniuta.gateway.reporting.service.LocalUserAuthenticationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * MVC tests for the authentication endpoint.
 */
@WebMvcTest(controllers = AuthController.class)
@Import({SecurityConfig.class, SuspiciousTrafficFilter.class})
@TestPropertySource(properties = {
        "app.security.jwt.secret-base64=c3VwZXItc2VjdXJlLWxvY2FsLWRldmVsb3BtZW50LWtleS1mb3Itand0LXRva2VuLTIwMjYtMDE=",
        "app.security.jwt.expiration-seconds=3600"
})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LocalUserAuthenticationService authenticationService;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private JwtDecoder jwtDecoder;
    @MockBean
    private SuspiciousRequestService suspiciousRequestService;
    @MockBean
    private ClientIpResolver clientIpResolver;
    @MockBean
    private SecurityTelemetry securityTelemetry;
    @MockBean
    private BrowserOriginValidationFilter browserOriginValidationFilter;

    @Test
    void shouldReturnTokenWhenCredentialsAreValid() throws Exception {
        when(authenticationService.isValid(any())).thenReturn(true);
        when(jwtService.issueToken(any(), any())).thenReturn("test-token");
        when(jwtService.expirationSeconds()).thenReturn(3600L);

        mockMvc.perform(post("/auth/token")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"report-admin","password":"changeit-admin"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("test-token"));
    }

    @Test
    void shouldRejectInvalidCredentials() throws Exception {
        when(authenticationService.isValid(any())).thenReturn(false);

        mockMvc.perform(post("/auth/token")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"report-admin","password":"bad"}
                                """))
                .andExpect(status().isUnauthorized());
    }
}
