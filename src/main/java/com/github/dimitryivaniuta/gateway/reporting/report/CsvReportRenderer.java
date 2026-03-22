package com.github.dimitryivaniuta.gateway.reporting.report;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Renders generated report rows as CSV.
 */
@Component
public class CsvReportRenderer {

    /**
     * Renders rows as a CSV artifact string.
     *
     * @param rows generated rows
     * @return CSV content
     */
    public String render(List<ReportDataRow> rows) {
        StringBuilder builder = new StringBuilder();
        builder.append("bookingReference,tenantId,reportCode,valueDate,counterparty,currency,amount,riskScore,status\n");
        for (ReportDataRow row : rows) {
            builder.append(csv(row.bookingReference())).append(',')
                    .append(csv(row.tenantId())).append(',')
                    .append(csv(row.reportCode())).append(',')
                    .append(csv(row.valueDate().toString())).append(',')
                    .append(csv(row.counterparty())).append(',')
                    .append(csv(row.currency())).append(',')
                    .append(csv(row.amount().toPlainString())).append(',')
                    .append(row.riskScore()).append(',')
                    .append(csv(row.status()))
                    .append('\n');
        }
        return builder.toString();
    }

    /**
     * Calculates the total amount of a generated artifact.
     *
     * @param rows generated rows
     * @return summed amount
     */
    public BigDecimal totalAmount(List<ReportDataRow> rows) {
        return rows.stream().map(ReportDataRow::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String csv(String value) {
        return '"' + value.replace("\"", "\"\"") + '"';
    }
}
