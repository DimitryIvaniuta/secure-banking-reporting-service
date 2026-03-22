package com.github.dimitryivaniuta.gateway.reporting.report;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * One generated row of reporting data.
 *
 * @param bookingReference business booking reference
 * @param tenantId owning tenant
 * @param reportCode report code
 * @param valueDate business value date
 * @param counterparty counterparty code
 * @param currency transaction currency
 * @param amount booking amount
 * @param riskScore synthetic risk score
 * @param status synthetic booking status
 */
public record ReportDataRow(
        String bookingReference,
        String tenantId,
        String reportCode,
        LocalDate valueDate,
        String counterparty,
        String currency,
        BigDecimal amount,
        int riskScore,
        String status
) {
}
