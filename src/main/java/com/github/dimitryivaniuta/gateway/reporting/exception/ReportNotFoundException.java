package com.github.dimitryivaniuta.gateway.reporting.exception;

/**
 * Signals that a report definition was not found.
 */
public class ReportNotFoundException extends RuntimeException {

    /**
     * Creates the exception.
     *
     * @param reportId missing report id
     */
    public ReportNotFoundException(Long reportId) {
        super("Report not found: id=" + reportId);
    }
}
