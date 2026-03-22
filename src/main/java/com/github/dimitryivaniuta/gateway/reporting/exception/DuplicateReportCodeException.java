package com.github.dimitryivaniuta.gateway.reporting.exception;

/**
 * Signals that a report code already exists.
 */
public class DuplicateReportCodeException extends RuntimeException {

    /**
     * Creates the exception.
     *
     * @param reportCode duplicate report code
     */
    public DuplicateReportCodeException(String reportCode) {
        super("Report code already exists: " + reportCode);
    }
}
