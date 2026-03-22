package com.github.dimitryivaniuta.gateway.reporting.report;

import com.github.dimitryivaniuta.gateway.reporting.domain.ReportDefinition;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * End-to-end report generation pipeline: parse criteria, build rows, render artifact, and compute metadata.
 */
@Service
@RequiredArgsConstructor
public class ReportGenerationService {

    private final ReportCriteriaParser reportCriteriaParser;
    private final ReportDataGenerator reportDataGenerator;
    private final CsvReportRenderer csvReportRenderer;

    /**
     * Generates a report artifact for the supplied definition.
     *
     * @param definition report definition
     * @return pipeline result
     */
    public ReportGenerationResult generate(ReportDefinition definition) {
        ReportCriteria criteria = reportCriteriaParser.parse(definition.getCriteriaJson());
        List<ReportDataRow> rows = reportDataGenerator.generate(definition, criteria);
        String artifactContent = csvReportRenderer.render(rows);
        String fileName = definition.getReportCode() + '-' + LocalDate.now() + ".csv";
        return new ReportGenerationResult(
                rows,
                "text/csv",
                fileName,
                artifactContent,
                checksum(artifactContent),
                csvReportRenderer.totalAmount(rows)
        );
    }

    private String checksum(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(input.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm is not available", exception);
        }
    }
}
