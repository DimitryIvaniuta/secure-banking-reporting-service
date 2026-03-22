package com.github.dimitryivaniuta.gateway.reporting.repository;

import com.github.dimitryivaniuta.gateway.reporting.domain.ReportArtifact;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for generated report artifacts.
 */
public interface ReportArtifactRepository extends JpaRepository<ReportArtifact, Long> {

    /**
     * Returns the generated artifact for a report execution.
     *
     * @param reportExecutionId execution identifier
     * @return matching artifact when present
     */
    Optional<ReportArtifact> findByReportExecutionId(Long reportExecutionId);
}
