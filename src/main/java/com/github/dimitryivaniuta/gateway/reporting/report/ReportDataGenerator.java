package com.github.dimitryivaniuta.gateway.reporting.report;

import com.github.dimitryivaniuta.gateway.reporting.domain.ReportDefinition;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.springframework.stereotype.Component;

/**
 * Generates deterministic synthetic reporting rows suitable for local and non-production environments.
 */
@Component
public class ReportDataGenerator {

    /**
     * Builds a deterministic data set from report criteria.
     *
     * @param definition report definition
     * @param criteria parsed criteria
     * @return generated rows
     */
    public List<ReportDataRow> generate(ReportDefinition definition, ReportCriteria criteria) {
        long seed = Math.abs((definition.getReportCode() + ':' + criteria.fromDate() + ':' + criteria.toDate()).hashCode());
        Random random = new Random(seed);
        long dateRange = Math.max(1, ChronoUnit.DAYS.between(criteria.fromDate(), criteria.toDate()) + 1);
        List<ReportDataRow> rows = new ArrayList<>(criteria.bookingCount());
        for (int index = 0; index < criteria.bookingCount(); index++) {
            BigDecimal amount = criteria.minimumAmount()
                    .add(BigDecimal.valueOf(50 + random.nextInt(9_950)))
                    .setScale(2, RoundingMode.HALF_UP);
            rows.add(new ReportDataRow(
                    definition.getReportCode() + '-' + String.format("%05d", index + 1),
                    definition.getTenantId(),
                    definition.getReportCode(),
                    criteria.fromDate().plusDays(Math.abs(random.nextLong()) % dateRange),
                    "CP-" + (100 + random.nextInt(900)),
                    criteria.currency(),
                    amount,
                    1 + random.nextInt(99),
                    random.nextBoolean() ? "BOOKED" : "SETTLED"
            ));
        }
        return rows;
    }
}
