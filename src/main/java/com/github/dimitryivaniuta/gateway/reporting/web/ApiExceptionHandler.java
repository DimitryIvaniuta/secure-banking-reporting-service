package com.github.dimitryivaniuta.gateway.reporting.web;

import com.github.dimitryivaniuta.gateway.reporting.exception.DuplicateReportCodeException;
import com.github.dimitryivaniuta.gateway.reporting.exception.ReportArtifactNotFoundException;
import com.github.dimitryivaniuta.gateway.reporting.exception.ReportNotFoundException;
import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Centralized API exception translation.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    /**
     * Handles missing reports.
     *
     * @param exception source exception
     * @return error response
     */
    @ExceptionHandler(ReportNotFoundException.class)
    public ProblemDetail handleNotFound(ReportNotFoundException exception) {
        return build(HttpStatus.NOT_FOUND, exception.getMessage());
    }


    /**
     * Handles missing generated artifacts.
     *
     * @param exception source exception
     * @return error response
     */
    @ExceptionHandler(ReportArtifactNotFoundException.class)
    public ProblemDetail handleArtifactNotFound(ReportArtifactNotFoundException exception) {
        return build(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    /**
     * Handles duplicate report code validation.
     *
     * @param exception source exception
     * @return error response
     */
    @ExceptionHandler(DuplicateReportCodeException.class)
    public ProblemDetail handleConflict(DuplicateReportCodeException exception) {
        return build(HttpStatus.CONFLICT, exception.getMessage());
    }

    /**
     * Handles payload validation.
     *
     * @param exception source exception
     * @return error response
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleBodyValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return build(HttpStatus.BAD_REQUEST, message.isBlank() ? "Validation failed" : message);
    }

    /**
     * Handles constraint validation.
     *
     * @param exception source exception
     * @return error response
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException exception) {
        return build(HttpStatus.BAD_REQUEST, exception.getMessage());
    }


    /**
     * Handles illegal arguments.
     *
     * @param exception source exception
     * @return error response
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException exception) {
        return build(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    private ProblemDetail build(HttpStatus status, String message) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, message);
        problemDetail.setTitle(status.getReasonPhrase());
        problemDetail.setType(URI.create("https://example.internal/problems/" + status.value()));
        return problemDetail;
    }
}
