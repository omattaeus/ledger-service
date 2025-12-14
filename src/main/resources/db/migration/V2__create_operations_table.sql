-- Migration: Create operations table
-- Purpose: Store financial operations with idempotency key (external_reference)

CREATE TABLE operations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    external_reference VARCHAR(255) NOT NULL UNIQUE,
    operation_type VARCHAR(20) NOT NULL CHECK (operation_type IN ('deposit', 'withdrawal', 'transfer')),
    status VARCHAR(20) NOT NULL CHECK (status IN ('processing', 'processed', 'ignored', 'failed')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP NULL
);

-- Indexes for performance and idempotency checks
CREATE UNIQUE INDEX idx_operations_external_reference ON operations(external_reference);
CREATE INDEX idx_operations_status ON operations(status);
CREATE INDEX idx_operations_created_at ON operations(created_at);
CREATE INDEX idx_operations_processed_at ON operations(processed_at) WHERE processed_at IS NOT NULL;

-- Comments for documentation
COMMENT ON TABLE operations IS 'Financial operations with idempotency guarantee via external_reference';
COMMENT ON COLUMN operations.external_reference IS 'UNIQUE external ID (from PSP/bank) - ensures idempotency';
COMMENT ON COLUMN operations.status IS 'processing: in progress, processed: completed, ignored: duplicate, failed: error';