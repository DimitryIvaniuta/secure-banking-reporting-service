package com.github.dimitryivaniuta.gateway.reporting.dto;

import com.github.dimitryivaniuta.gateway.reporting.domain.ReportStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * API response representing a report execution.
 */
public record ReportExecutionResponse(
        Long id,
        String executionReference,
        ReportStatus status,
        Long recordCount,
        String checksum,
        BigDecimal totalAmount,
        String resultPayloadJson,
        String requestedBy,
        OffsetDateTime startedAt,
        OffsetDateTime completedAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        String artifactFileName,
        String artifactContentType,
        String artifactChecksum,
        Long artifactSizeBytes
) {
}
