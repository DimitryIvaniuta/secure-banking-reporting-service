package com.github.dimitryivaniuta.gateway.reporting.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.dimitryivaniuta.gateway.reporting.config.SecurityProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Tests browser origin validation.
 */
class BrowserOriginValidationFilterTest {

    private BrowserOriginValidationFilter filter;

    @BeforeEach
    void setUp() {
        SecurityProperties properties = new SecurityProperties();
        properties.getBrowser().setAllowedOrigins(List.of("http://localhost:3000"));
        filter = new BrowserOriginValidationFilter(properties);
    }

    @Test
    void shouldAllowConfiguredOrigin() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/portal/forms/report");
        request.addHeader("Origin", "http://localhost:3000");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = Mockito.mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        Mockito.verify(chain).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    }

    @Test
    void shouldRejectMissingOriginAndReferer() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/portal/forms/report");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, Mockito.mock(FilterChain.class));

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FORBIDDEN);
        assertThat(response.getContentAsString()).contains("untrusted Origin/Referer");
    }
}
