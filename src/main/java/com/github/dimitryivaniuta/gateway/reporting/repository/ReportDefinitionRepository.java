package com.github.dimitryivaniuta.gateway.reporting.repository;

import com.github.dimitryivaniuta.gateway.reporting.domain.ReportDefinition;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for report definitions.
 */
public interface ReportDefinitionRepository extends JpaRepository<ReportDefinition, Long> {

    /**
     * Finds a report by report code.
     *
     * @param reportCode business report code
     * @return report if present
     */
    Optional<ReportDefinition> findByReportCode(String reportCode);
}
