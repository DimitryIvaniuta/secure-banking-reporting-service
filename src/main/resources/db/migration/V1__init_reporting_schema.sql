CREATE TABLE report_definition (
    id BIGSERIAL PRIMARY KEY,
    report_code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    report_type VARCHAR(40) NOT NULL,
    tenant_id VARCHAR(64) NOT NULL,
    criteria_json TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE report_execution (
    id BIGSERIAL PRIMARY KEY,
    report_definition_id BIGINT NOT NULL REFERENCES report_definition(id),
    execution_reference VARCHAR(80) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL,
    record_count BIGINT NOT NULL,
    checksum VARCHAR(128) NOT NULL,
    total_amount NUMERIC(19,4) NOT NULL,
    result_payload_json TEXT NOT NULL,
    requested_by VARCHAR(100) NOT NULL,
    started_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_report_definition_tenant_id ON report_definition(tenant_id);
CREATE INDEX idx_report_execution_report_id ON report_execution(report_definition_id);
CREATE INDEX idx_report_execution_created_at ON report_execution(created_at DESC);
