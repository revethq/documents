CREATE TABLE revet_tenant_capabilities (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(255) NOT NULL,
    capability_id VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT true,
    reason TEXT,
    created_on TIMESTAMPTZ NOT NULL,
    updated_on TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_revet_tenant_capabilities_tenant_capability UNIQUE (tenant_id, capability_id)
);

CREATE INDEX idx_revet_tenant_capabilities_tenant_id ON revet_tenant_capabilities(tenant_id);
CREATE INDEX idx_revet_tenant_capabilities_capability_id ON revet_tenant_capabilities(capability_id);
