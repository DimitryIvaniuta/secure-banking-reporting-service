package com.github.dimitryivaniuta.gateway.reporting.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request used to trigger a report execution.
 */
public record RunReportRequest(@NotBlank String requestedBy) {
}
