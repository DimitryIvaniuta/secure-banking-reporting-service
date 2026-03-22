package com.github.dimitryivaniuta.gateway.reporting.report;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Parses report criteria JSON into a validated internal model.
 */
@Component
@RequiredArgsConstructor
public class ReportCriteriaParser {

    private final ObjectMapper objectMapper;

    /**
     * Parses report criteria JSON and applies production-style defaults.
     *
     * @param criteriaJson raw criteria JSON
     * @return parsed criteria
     */
    public ReportCriteria parse(String criteriaJson) {
        try {
            JsonNode node = objectMapper.readTree(criteriaJson);
            LocalDate fromDate = readDate(node, "fromDate", LocalDate.now().minusDays(7));
            LocalDate toDate = readDate(node, "toDate", LocalDate.now());
            int bookingCount = node.path("bookingCount").asInt(25);
            if (bookingCount < 1) {
                bookingCount = 1;
            }
            if (bookingCount > 1_000) {
                bookingCount = 1_000;
            }
            String currency = node.path("currency").asText("EUR");
            BigDecimal minimumAmount = node.hasNonNull("minimumAmount")
                    ? node.path("minimumAmount").decimalValue()
                    : new BigDecimal("100.00");
            return new ReportCriteria(fromDate, toDate, bookingCount, currency, minimumAmount);
        } catch (Exception exception) {
            throw new IllegalArgumentException("Invalid report criteria JSON", exception);
        }
    }

    private LocalDate readDate(JsonNode node, String fieldName, LocalDate defaultValue) {
        String value = node.path(fieldName).asText(null);
        return value == null || value.isBlank() ? defaultValue : LocalDate.parse(value);
    }
}
