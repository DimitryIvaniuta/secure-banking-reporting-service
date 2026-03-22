package com.github.dimitryivaniuta.gateway.reporting.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dimitryivaniuta.gateway.reporting.domain.ReportArtifact;
import com.github.dimitryivaniuta.gateway.reporting.domain.ReportDefinition;
import com.github.dimitryivaniuta.gateway.reporting.domain.ReportExecution;
import com.github.dimitryivaniuta.gateway.reporting.domain.ReportStatus;
import com.github.dimitryivaniuta.gateway.reporting.dto.CreateReportRequest;
import com.github.dimitryivaniuta.gateway.reporting.dto.ReportDefinitionResponse;
import com.github.dimitryivaniuta.gateway.reporting.dto.RunReportRequest;
import com.github.dimitryivaniuta.gateway.reporting.exception.DuplicateReportCodeException;
import com.github.dimitryivaniuta.gateway.reporting.exception.ReportArtifactNotFoundException;
import com.github.dimitryivaniuta.gateway.reporting.exception.ReportNotFoundException;
import com.github.dimitryivaniuta.gateway.reporting.kafka.ReportEventPublisher;
import com.github.dimitryivaniuta.gateway.reporting.report.ReportGenerationResult;
import com.github.dimitryivaniuta.gateway.reporting.report.ReportGenerationService;
import com.github.dimitryivaniuta.gateway.reporting.repository.ReportArtifactRepository;
import com.github.dimitryivaniuta.gateway.reporting.repository.ReportDefinitionRepository;
import com.github.dimitryivaniuta.gateway.reporting.repository.ReportExecutionRepository;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Main business service for report lifecycle operations.
 */
@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportDefinitionRepository reportDefinitionRepository;
    private final ReportExecutionRepository reportExecutionRepository;
    private final ReportArtifactRepository reportArtifactRepository;
    private final ReportMapper reportMapper;
    private final ReportEventPublisher reportEventPublisher;
    private final StringRedisTemplate stringRedisTemplate;
    private final ReportGenerationService reportGenerationService;
    private final ObjectMapper objectMapper;

    /**
     * Creates a new report definition.
     *
     * @param request API request
     * @param createdBy authenticated operator
     * @return created report response
     */
    @Transactional
    public ReportDefinitionResponse createReport(CreateReportRequest request, String createdBy) {
        reportDefinitionRepository.findByReportCode(request.reportCode())
                .ifPresent(existing -> {
                    throw new DuplicateReportCodeException(request.reportCode());
                });

        ReportDefinition definition = new ReportDefinition();
        definition.setReportCode(request.reportCode());
        definition.setName(request.name());
        definition.setReportType(request.reportType());
        definition.setTenantId(request.tenantId());
        definition.setCriteriaJson(request.criteriaJson());
        definition.setStatus(ReportStatus.ACTIVE);
        definition.setCreatedBy(createdBy);

        ReportDefinition saved = reportDefinitionRepository.save(definition);
        reportEventPublisher.publishReportCreated(saved);
        cacheReportSummary(saved, 0, null);
        return reportMapper.toResponse(saved, List.of());
    }

    /**
     * Retrieves a report definition including recent runs.
     *
     * @param reportId report id
     * @return report response
     */
    @Transactional(readOnly = true)
    public ReportDefinitionResponse getReport(Long reportId) {
        ReportDefinition definition = reportDefinitionRepository.findById(reportId)
                .orElseThrow(() -> new ReportNotFoundException(reportId));
        List<ReportExecution> executions = reportExecutionRepository.findByReportDefinitionIdOrderByCreatedAtDesc(reportId);
        return reportMapper.toResponse(definition, executions);
    }

    /**
     * Returns all reports without filtering.
     *
     * @return report responses
     */
    @Transactional(readOnly = true)
    public List<ReportDefinitionResponse> listReports() {
        return reportDefinitionRepository.findAll().stream()
                .map(definition -> reportMapper.toResponse(
                        definition,
                        reportExecutionRepository.findByReportDefinitionIdOrderByCreatedAtDesc(definition.getId())
                ))
                .toList();
    }

    /**
     * Runs a report through the generation pipeline.
     *
     * @param reportId report id
     * @param request run request
     * @return updated report response
     */
    @Transactional
    public ReportDefinitionResponse runReport(Long reportId, RunReportRequest request) {
        ReportDefinition definition = reportDefinitionRepository.findById(reportId)
                .orElseThrow(() -> new ReportNotFoundException(reportId));

        ReportExecution execution = new ReportExecution();
        execution.setReportDefinition(definition);
        execution.setExecutionReference("run-" + UUID.randomUUID());
        execution.setStatus(ReportStatus.RUNNING);
        execution.setRecordCount(0L);
        execution.setRequestedBy(request.requestedBy());
        execution.setChecksum("pending");
        execution.setTotalAmount(java.math.BigDecimal.ZERO);
        execution.setResultPayloadJson("{}");
        execution.setStartedAt(OffsetDateTime.now());
        reportExecutionRepository.save(execution);

        ReportGenerationResult generationResult = reportGenerationService.generate(definition);
        execution.setStatus(ReportStatus.COMPLETED);
        execution.setRecordCount((long) generationResult.rows().size());
        execution.setChecksum(generationResult.checksum());
        execution.setTotalAmount(generationResult.totalAmount());
        execution.setResultPayloadJson(buildResultPayload(definition, execution, generationResult));
        execution.setCompletedAt(OffsetDateTime.now());
        reportExecutionRepository.save(execution);

        ReportArtifact artifact = new ReportArtifact();
        artifact.setReportExecution(execution);
        artifact.setFileName(generationResult.fileName());
        artifact.setContentType(generationResult.contentType());
        artifact.setContentText(generationResult.artifactContent());
        artifact.setChecksum(generationResult.checksum());
        artifact.setSizeBytes((long) generationResult.artifactContent().getBytes(StandardCharsets.UTF_8).length);
        reportArtifactRepository.save(artifact);
        execution.setArtifact(artifact);

        cacheReportSummary(definition, execution.getRecordCount(), execution.getExecutionReference());
        reportEventPublisher.publishExecutionCompleted(execution);
        reportEventPublisher.publishArtifactGenerated(execution, artifact);
        return getReport(reportId);
    }

    /**
     * Returns a generated artifact for download.
     *
     * @param executionId execution id
     * @return artifact
     */
    @Transactional(readOnly = true)
    public ReportArtifact getArtifact(Long executionId) {
        return reportArtifactRepository.findByReportExecutionId(executionId)
                .orElseThrow(() -> new ReportArtifactNotFoundException(executionId));
    }

    private void cacheReportSummary(ReportDefinition definition, long recordCount, String executionReference) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("reportId", definition.getId());
        summary.put("reportCode", definition.getReportCode());
        summary.put("tenantId", definition.getTenantId());
        summary.put("recordCount", recordCount);
        summary.put("lastExecutionReference", executionReference);
        try {
            stringRedisTemplate.opsForValue().set(cacheKey(definition.getId()), objectMapper.writeValueAsString(summary));
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to cache report summary", exception);
        }
    }

    private String cacheKey(Long reportId) {
        return "report:summary:" + reportId;
    }

    private String buildResultPayload(ReportDefinition definition,
                                      ReportExecution execution,
                                      ReportGenerationResult generationResult) {
        Map<String, Object> payload = Map.of(
                "result", "SUCCESS",
                "reportCode", definition.getReportCode(),
                "executionReference", execution.getExecutionReference(),
                "generatedRows", generationResult.rows().size(),
                "artifactFileName", generationResult.fileName(),
                "artifactChecksum", generationResult.checksum()
        );
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to serialize report result payload", exception);
        }
    }
}
