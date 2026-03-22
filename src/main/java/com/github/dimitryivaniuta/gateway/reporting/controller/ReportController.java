package com.github.dimitryivaniuta.gateway.reporting.controller;

import com.github.dimitryivaniuta.gateway.reporting.domain.ReportArtifact;
import com.github.dimitryivaniuta.gateway.reporting.dto.CreateReportRequest;
import com.github.dimitryivaniuta.gateway.reporting.dto.ReportDefinitionResponse;
import com.github.dimitryivaniuta.gateway.reporting.dto.RunReportRequest;
import com.github.dimitryivaniuta.gateway.reporting.service.ReportService;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Main REST API for report definitions and executions.
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * Creates a report definition.
     *
     * @param request API request
     * @param principal authenticated principal
     * @return created response
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReportDefinitionResponse createReport(@Valid @RequestBody CreateReportRequest request, Principal principal) {
        return reportService.createReport(request, principal.getName());
    }

    /**
     * Returns a report by id.
     *
     * @param reportId report id
     * @return report response
     */
    @GetMapping("/{reportId}")
    public ReportDefinitionResponse getReport(@PathVariable Long reportId) {
        return reportService.getReport(reportId);
    }

    /**
     * Lists all report definitions.
     *
     * @return report responses
     */
    @GetMapping
    public List<ReportDefinitionResponse> listReports() {
        return reportService.listReports();
    }

    /**
     * Executes a report.
     *
     * @param reportId report id
     * @param request run request
     * @return updated report state
     */
    @PostMapping("/{reportId}/run")
    public ReportDefinitionResponse runReport(@PathVariable Long reportId, @Valid @RequestBody RunReportRequest request) {
        return reportService.runReport(reportId, request);
    }

    /**
     * Downloads a generated report artifact.
     *
     * @param executionId execution id
     * @return CSV download response
     */
    @GetMapping("/executions/{executionId}/artifact")
    public ResponseEntity<byte[]> downloadArtifact(@PathVariable Long executionId) {
        ReportArtifact artifact = reportService.getArtifact(executionId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(artifact.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(artifact.getFileName(), StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .body(artifact.getContentText().getBytes(StandardCharsets.UTF_8));
    }
}
