package com.github.dimitryivaniuta.gateway.reporting.report;

import java.math.BigDecimal;
import java.util.List;

/**
 * Combined output of the report generation pipeline.
 *
 * @param rows generated rows
 * @param contentType artifact MIME type
 * @param fileName artifact file name
 * @param artifactContent rendered file payload
 * @param checksum artifact checksum
 * @param totalAmount generated total amount
 */
public record ReportGenerationResult(
        List<ReportDataRow> rows,
        String contentType,
        String fileName,
        String artifactContent,
        String checksum,
        BigDecimal totalAmount
) {
}
