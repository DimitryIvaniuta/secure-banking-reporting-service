package com.github.dimitryivaniuta.gateway.reporting.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.dimitryivaniuta.gateway.reporting.security.JwtService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * End-to-end integration test for the report lifecycle using Testcontainers.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ReportFlowIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16.4"))
            .withDatabaseName("reporting")
            .withUsername("reporting_user")
            .withPassword("reporting_password");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7.2.5"))
            .withExposedPorts(6379)
            .waitingFor(Wait.forListeningPort());

    @Container
    static GenericContainer<?> kafka = new GenericContainer<>(DockerImageName.parse("apache/kafka:3.9.0"))
            .withEnv("KAFKA_NODE_ID", "1")
            .withEnv("KAFKA_PROCESS_ROLES", "broker,controller")
            .withEnv("KAFKA_LISTENERS", "PLAINTEXT://:9092,CONTROLLER://:9093")
            .withEnv("KAFKA_ADVERTISED_LISTENERS", "PLAINTEXT://localhost:9092")
            .withEnv("KAFKA_CONTROLLER_LISTENER_NAMES", "CONTROLLER")
            .withEnv("KAFKA_LISTENER_SECURITY_PROTOCOL_MAP", "CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT")
            .withEnv("KAFKA_CONTROLLER_QUORUM_VOTERS", "1@localhost:9093")
            .withEnv("KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR", "1")
            .withEnv("KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR", "1")
            .withEnv("KAFKA_TRANSACTION_STATE_LOG_MIN_ISR", "1")
            .withEnv("KAFKA_LOG_DIRS", "/tmp/kraft-combined-logs")
            .withEnv("KAFKA_AUTO_CREATE_TOPICS_ENABLE", "true")
            .withCommand("bash", "-c", "export CLUSTER_ID=4L6g3nShT-eMCtK--X86sw && /etc/kafka/docker/run")
            .withExposedPorts(9092)
            .waitingFor(Wait.forListeningPort());

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
        registry.add("spring.kafka.bootstrap-servers", () -> kafka.getHost() + ":" + kafka.getMappedPort(9092));
        registry.add("server.servlet.session.cookie.secure", () -> false);
        registry.add("management.health.kafka.enabled", () -> false);
    }

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    JwtService jwtService;

    @Test
    void shouldCreateRunAndDownloadArtifact() {
        String token = jwtService.issueToken("report-admin", List.of("ADMIN", "VIEWER"));

        HttpHeaders headers = authorizedHeaders(token);
        ResponseEntity<Map<String, Object>> createResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/reports",
                HttpMethod.POST,
                new HttpEntity<>(Map.of(
                        "reportCode", "RPT-IT-001",
                        "name", "Integration Report",
                        "reportType", "AML",
                        "tenantId", "tenant-it",
                        "criteriaJson", "{\"fromDate\":\"2026-01-01\",\"toDate\":\"2026-01-03\",\"bookingCount\":3,\"currency\":\"EUR\",\"minimumAmount\":100.00}"
                ), headers),
                new ParameterizedTypeReference<>() {
                });

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long reportId = ((Number) createResponse.getBody().get("id")).longValue();

        ResponseEntity<Map<String, Object>> runResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/reports/" + reportId + "/run",
                HttpMethod.POST,
                new HttpEntity<>(Map.of("requestedBy", "integration-tester"), headers),
                new ParameterizedTypeReference<>() {
                });

        assertThat(runResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<Map<String, Object>> executions = (List<Map<String, Object>>) runResponse.getBody().get("recentExecutions");
        assertThat(executions).isNotEmpty();
        Long executionId = ((Number) executions.getFirst().get("id")).longValue();

        ResponseEntity<String> artifactResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/reports/executions/" + executionId + "/artifact",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        assertThat(artifactResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(artifactResponse.getHeaders().getContentType()).isEqualTo(MediaType.parseMediaType("text/csv"));
        assertThat(artifactResponse.getBody()).contains("bookingReference,tenantId,reportCode");
        assertThat(artifactResponse.getBody()).contains("RPT-IT-001");
    }

    private HttpHeaders defaultJsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private HttpHeaders authorizedHeaders(String token) {
        HttpHeaders headers = defaultJsonHeaders();
        headers.setBearerAuth(token);
        return headers;
    }
}
