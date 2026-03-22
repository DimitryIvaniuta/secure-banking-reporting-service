package com.github.dimitryivaniuta.gateway.reporting.dto;

import com.github.dimitryivaniuta.gateway.reporting.domain.ReportStatus;
import com.github.dimitryivaniuta.gateway.reporting.domain.ReportType;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * API response representing a report definition and its recent runs.
 */
public record ReportDefinitionResponse(
        Long id,
        String reportCode,
        String name,
        ReportType reportType,
        String tenantId,
        String criteriaJson,
        ReportStatus status,
        String createdBy,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        List<ReportExecutionResponse> recentExecutions
) {
}
