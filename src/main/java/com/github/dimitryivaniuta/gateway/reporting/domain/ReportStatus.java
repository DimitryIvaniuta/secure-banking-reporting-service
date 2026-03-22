package com.github.dimitryivaniuta.gateway.reporting.domain;

/**
 * Lifecycle status of a report definition or generated run.
 */
public enum ReportStatus {
    DRAFT,
    ACTIVE,
    RUNNING,
    COMPLETED,
    FAILED
}
