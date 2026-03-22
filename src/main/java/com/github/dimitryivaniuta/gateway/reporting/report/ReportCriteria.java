package com.github.dimitryivaniuta.gateway.reporting.report;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Parsed report criteria used by the generation pipeline.
 *
 * @param fromDate inclusive business start date
 * @param toDate inclusive business end date
 * @param bookingCount target synthetic booking count
 * @param currency settlement currency
 * @param minimumAmount minimum generated booking amount
 */
public record ReportCriteria(
        LocalDate fromDate,
        LocalDate toDate,
        int bookingCount,
        String currency,
        BigDecimal minimumAmount
) {
}
