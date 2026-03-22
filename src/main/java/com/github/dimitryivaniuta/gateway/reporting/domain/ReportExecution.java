package com.github.dimitryivaniuta.gateway.reporting.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * Persistent run instance of a report execution.
 */
@Getter
@Setter
@Entity
@Table(name = "report_execution")
public class ReportExecution {

    /** Database identifier. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Report definition owning this execution. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "report_definition_id", nullable = false)
    private ReportDefinition reportDefinition;


    /** Generated artifact for this execution. */
    @OneToOne(mappedBy = "reportExecution", fetch = FetchType.LAZY)
    private ReportArtifact artifact;

    /** Execution correlation id. */
    @Column(name = "execution_reference", nullable = false, unique = true, length = 80)
    private String executionReference;

    /** Current execution status. */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReportStatus status;

    /** Generated row count. */
    @Column(name = "record_count", nullable = false)
    private Long recordCount;

    /** Generated checksum or hash surrogate. */
    @Column(name = "checksum", nullable = false, length = 128)
    private String checksum;

    /** Total amount covered by the report output. */
    @Column(name = "total_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalAmount;

    /** Runtime result payload as JSON. */
    @Column(name = "result_payload_json", nullable = false, columnDefinition = "text")
    private String resultPayloadJson;

    /** Who initiated the run. */
    @Column(name = "requested_by", nullable = false, length = 100)
    private String requestedBy;

    /** Execution start time. */
    @Column(name = "started_at", nullable = false)
    private OffsetDateTime startedAt;

    /** Execution completion time. */
    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    /** Entity creation timestamp. */
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    /** Entity update timestamp. */
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    /** Initializes audit fields on creation. */
    @PrePersist
    public void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.startedAt == null) {
            this.startedAt = now;
        }
    }

    /** Updates audit fields on modification. */
    @PreUpdate
    public void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}
