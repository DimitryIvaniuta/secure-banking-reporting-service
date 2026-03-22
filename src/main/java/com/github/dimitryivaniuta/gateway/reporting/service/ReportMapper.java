package com.github.dimitryivaniuta.gateway.reporting.service;

import com.github.dimitryivaniuta.gateway.reporting.domain.ReportArtifact;
import com.github.dimitryivaniuta.gateway.reporting.domain.ReportDefinition;
import com.github.dimitryivaniuta.gateway.reporting.domain.ReportExecution;
import com.github.dimitryivaniuta.gateway.reporting.dto.ReportDefinitionResponse;
import com.github.dimitryivaniuta.gateway.reporting.dto.ReportExecutionResponse;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Maps domain entities to API DTOs.
 */
@Component
public class ReportMapper {

    /**
     * Maps a report definition.
     *
     * @param definition source definition
     * @param executions recent executions
     * @return mapped response
     */
    public ReportDefinitionResponse toResponse(ReportDefinition definition, List<ReportExecution> executions) {
        return new ReportDefinitionResponse(
                definition.getId(),
                definition.getReportCode(),
                definition.getName(),
                definition.getReportType(),
                definition.getTenantId(),
                definition.getCriteriaJson(),
                definition.getStatus(),
                definition.getCreatedBy(),
                definition.getCreatedAt(),
                definition.getUpdatedAt(),
                executions.stream().map(this::toResponse).toList()
        );
    }

    /**
     * Maps a report execution.
     *
     * @param execution source execution
     * @return mapped response
     */
    public ReportExecutionResponse toResponse(ReportExecution execution) {
        ReportArtifact artifact = execution.getArtifact();
        return new ReportExecutionResponse(
                execution.getId(),
                execution.getExecutionReference(),
                execution.getStatus(),
                execution.getRecordCount(),
                execution.getChecksum(),
                execution.getTotalAmount(),
                execution.getResultPayloadJson(),
                execution.getRequestedBy(),
                execution.getStartedAt(),
                execution.getCompletedAt(),
                execution.getCreatedAt(),
                execution.getUpdatedAt(),
                artifact != null ? artifact.getFileName() : null,
                artifact != null ? artifact.getContentType() : null,
                artifact != null ? artifact.getChecksum() : null,
                artifact != null ? artifact.getSizeBytes() : null
        );
    }
}
