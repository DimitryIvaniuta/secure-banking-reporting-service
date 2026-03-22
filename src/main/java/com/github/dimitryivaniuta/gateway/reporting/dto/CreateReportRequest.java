package com.github.dimitryivaniuta.gateway.reporting.dto;

import com.github.dimitryivaniuta.gateway.reporting.domain.ReportType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request used to create a report definition.
 */
public record CreateReportRequest(
        @NotBlank @Size(max = 64) String reportCode,
        @NotBlank @Size(max = 200) String name,
        @NotNull ReportType reportType,
        @NotBlank @Size(max = 64) String tenantId,
        @NotBlank String criteriaJson
) {
}
