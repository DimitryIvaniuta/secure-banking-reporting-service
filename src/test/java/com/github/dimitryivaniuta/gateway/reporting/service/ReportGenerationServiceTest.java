package com.github.dimitryivaniuta.gateway.reporting.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dimitryivaniuta.gateway.reporting.domain.ReportDefinition;
import com.github.dimitryivaniuta.gateway.reporting.domain.ReportType;
import com.github.dimitryivaniuta.gateway.reporting.report.CsvReportRenderer;
import com.github.dimitryivaniuta.gateway.reporting.report.ReportCriteriaParser;
import com.github.dimitryivaniuta.gateway.reporting.report.ReportDataGenerator;
import com.github.dimitryivaniuta.gateway.reporting.report.ReportGenerationService;
import org.junit.jupiter.api.Test;

/**
 * Tests the report generation pipeline.
 */
class ReportGenerationServiceTest {

    @Test
    void shouldGenerateCsvArtifactFromCriteriaJson() {
        ReportDefinition definition = new ReportDefinition();
        definition.setReportCode("RPT-PIPE-001");
        definition.setTenantId("tenant-a");
        definition.setReportType(ReportType.AML);
        definition.setCriteriaJson("""
                {
                  "fromDate":"2026-02-01",
                  "toDate":"2026-02-03",
                  "bookingCount":4,
                  "currency":"EUR",
                  "minimumAmount":150.50
                }
                """);

        ReportGenerationService service = new ReportGenerationService(
                new ReportCriteriaParser(new ObjectMapper()),
                new ReportDataGenerator(),
                new CsvReportRenderer()
        );

        var result = service.generate(definition);

        assertThat(result.rows()).hasSize(4);
        assertThat(result.contentType()).isEqualTo("text/csv");
        assertThat(result.fileName()).endsWith(".csv");
        assertThat(result.artifactContent()).contains("bookingReference,tenantId,reportCode");
        assertThat(result.artifactContent()).contains("RPT-PIPE-001");
        assertThat(result.totalAmount()).isPositive();
    }
}
