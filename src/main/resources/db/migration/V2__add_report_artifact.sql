CREATE TABLE report_artifact (
    id BIGSERIAL PRIMARY KEY,
    report_execution_id BIGINT NOT NULL UNIQUE REFERENCES report_execution(id) ON DELETE CASCADE,
    file_name VARCHAR(180) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    content_text TEXT NOT NULL,
    checksum VARCHAR(128) NOT NULL,
    size_bytes BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_report_artifact_execution_id ON report_artifact(report_execution_id);
