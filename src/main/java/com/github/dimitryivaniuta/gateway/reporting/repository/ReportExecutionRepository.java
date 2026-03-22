package com.github.dimitryivaniuta.gateway.reporting.repository;

import com.github.dimitryivaniuta.gateway.reporting.domain.ReportExecution;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository for report executions.
 */
public interface ReportExecutionRepository extends JpaRepository<ReportExecution, Long> {

    /**
     * Returns all executions for a report definition ordered by newest first.
     *
     * @param reportDefinitionId owning report id
     * @return matching executions
     */
    @EntityGraph(attributePaths = "artifact")
    List<ReportExecution> findByReportDefinitionIdOrderByCreatedAtDesc(Long reportDefinitionId);

    /**
     * Returns a single execution with its associations.
     *
     * @param id execution id
     * @return execution with artifact and definition
     */
    @Query("""
            select execution
            from ReportExecution execution
            left join fetch execution.artifact
            left join fetch execution.reportDefinition
            where execution.id = :id
            """)
    Optional<ReportExecution> findDetailedById(@Param("id") Long id);
}
