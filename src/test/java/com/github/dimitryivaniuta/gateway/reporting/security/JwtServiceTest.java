package com.github.dimitryivaniuta.gateway.reporting.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Tests JWT issuance and parsing.
 */
class JwtServiceTest {

    @Test
    void shouldIssueAndParseToken() {
        JwtService jwtService = new JwtService(
                "c3VwZXItc2VjdXJlLWxvY2FsLWRldmVsb3BtZW50LWtleS1mb3Itand0LXRva2VuLTIwMjYtMDE=",
                3600
        );

        String token = jwtService.issueToken("report-admin", List.of("ADMIN", "VIEWER"));

        assertThat(jwtService.parse(token).getSubject()).isEqualTo("report-admin");
        assertThat(jwtService.parse(token).get("roles", List.class)).contains("ADMIN", "VIEWER");
    }
}
