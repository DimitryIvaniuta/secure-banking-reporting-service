package com.github.dimitryivaniuta.gateway.reporting.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dimitryivaniuta.gateway.reporting.domain.ReportArtifact;
import com.github.dimitryivaniuta.gateway.reporting.domain.ReportDefinition;
import com.github.dimitryivaniuta.gateway.reporting.domain.ReportExecution;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes report lifecycle events to Kafka for downstream analytics and monitoring.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReportEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Publishes a report definition created event.
     *
     * @param reportDefinition created report
     */
    public void publishReportCreated(ReportDefinition reportDefinition) {
        send("report.created", reportDefinition.getReportCode(), Map.of(
                "reportId", reportDefinition.getId(),
                "reportCode", reportDefinition.getReportCode(),
                "tenantId", reportDefinition.getTenantId(),
                "status", reportDefinition.getStatus().name()
        ));
    }

    /**
     * Publishes a report execution completed event.
     *
     * @param execution completed execution
     */
    public void publishExecutionCompleted(ReportExecution execution) {
        send("report.execution.completed", execution.getExecutionReference(), Map.of(
                "executionId", execution.getId(),
                "executionReference", execution.getExecutionReference(),
                "reportCode", execution.getReportDefinition().getReportCode(),
                "status", execution.getStatus().name(),
                "recordCount", execution.getRecordCount(),
                "totalAmount", execution.getTotalAmount().toPlainString()
        ));
    }

    /**
     * Publishes a report artifact created event.
     *
     * @param execution completed execution
     * @param artifact generated artifact
     */
    public void publishArtifactGenerated(ReportExecution execution, ReportArtifact artifact) {
        send("report.artifact.generated", execution.getExecutionReference(), Map.of(
                "executionId", execution.getId(),
                "executionReference", execution.getExecutionReference(),
                "fileName", artifact.getFileName(),
                "contentType", artifact.getContentType(),
                "sizeBytes", artifact.getSizeBytes(),
                "checksum", artifact.getChecksum()
        ));
    }

    private void send(String topic, String key, Map<String, Object> payload) {
        try {
            kafkaTemplate.send(topic, key, objectMapper.writeValueAsString(payload));
        } catch (JsonProcessingException exception) {
            log.warn("Failed to serialize Kafka event for topic {} and key {}", topic, key, exception);
        }
    }
}
