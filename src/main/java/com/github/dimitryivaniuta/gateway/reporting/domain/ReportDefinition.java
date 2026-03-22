package com.github.dimitryivaniuta.gateway.reporting.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * Persistent definition of a banking report that can be executed on demand.
 */
@Getter
@Setter
@Entity
@Table(name = "report_definition")
public class ReportDefinition {

    /** Database identifier. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Human-friendly report code used by operators and integrations. */
    @Column(name = "report_code", nullable = false, unique = true, length = 64)
    private String reportCode;

    /** Report display name. */
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    /** Report type. */
    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false, length = 40)
    private ReportType reportType;

    /** Owning tenant or business unit. */
    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    /** JSON payload describing report criteria. */
    @Column(name = "criteria_json", nullable = false, columnDefinition = "text")
    private String criteriaJson;

    /** Current report status. */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReportStatus status;

    /** Report creator. */
    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;

    /** Creation timestamp. */
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    /** Last update timestamp. */
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    /** Initializes audit fields on creation. */
    @PrePersist
    public void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    /** Updates audit timestamp on entity modification. */
    @PreUpdate
    public void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}
