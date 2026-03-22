package com.github.dimitryivaniuta.gateway.reporting.exception;

/**
 * Raised when a report artifact is requested but does not exist.
 */
public class ReportArtifactNotFoundException extends RuntimeException {

    /**
     * Creates the exception.
     *
     * @param executionId execution id
     */
    public ReportArtifactNotFoundException(Long executionId) {
        super("Generated report artifact was not found for execution id: " + executionId);
    }
}
