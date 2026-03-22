package com.github.dimitryivaniuta.gateway.reporting.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * Persisted report artifact generated from a report execution.
 */
@Getter
@Setter
@Entity
@Table(name = "report_artifact")
public class ReportArtifact {

    /** Database identifier. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Owning report execution. */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "report_execution_id", nullable = false, unique = true)
    private ReportExecution reportExecution;

    /** Generated file name presented to callers. */
    @Column(name = "file_name", nullable = false, length = 180)
    private String fileName;

    /** MIME type of the generated artifact. */
    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    /** Artifact character payload for generated text reports. */
    @Column(name = "content_text", nullable = false, columnDefinition = "text")
    private String contentText;

    /** Generated artifact checksum. */
    @Column(name = "checksum", nullable = false, length = 128)
    private String checksum;

    /** Artifact size in bytes. */
    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;

    /** Creation timestamp. */
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    /** Last update timestamp. */
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    /** Initializes timestamps on creation. */
    @PrePersist
    public void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    /** Updates timestamp on modification. */
    @PreUpdate
    public void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}
