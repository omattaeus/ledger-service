-- Migration: Create reconciliation_records table
-- Purpose: Track reconciliation attempts and detect divergences between internal and external balances

CREATE TABLE reconciliation_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id UUID NOT NULL REFERENCES accounts(id) ON DELETE RESTRICT,
    reconciliation_date DATE NOT NULL,
    expected_balance NUMERIC(19, 4) NOT NULL,
    calculated_balance NUMERIC(19, 4) NOT NULL,
    difference NUMERIC(19, 4) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('match', 'mismatch')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for queries and reporting
CREATE INDEX idx_reconciliation_account_id ON reconciliation_records(account_id);
CREATE INDEX idx_reconciliation_date ON reconciliation_records(reconciliation_date);
CREATE INDEX idx_reconciliation_status ON reconciliation_records(status);
CREATE INDEX idx_reconciliation_account_date ON reconciliation_records(account_id, reconciliation_date DESC);

-- Comments for documentation
COMMENT ON TABLE reconciliation_records IS 'History of reconciliation attempts - detects divergences without auto-correction';
COMMENT ON COLUMN reconciliation_records.expected_balance IS 'Balance according to external source (PSP/bank)';
COMMENT ON COLUMN reconciliation_records.calculated_balance IS 'Balance calculated as SUM(entries)';
COMMENT ON COLUMN reconciliation_records.difference IS 'expected_balance - calculated_balance (positive = missing money internally)';
COMMENT ON COLUMN reconciliation_records.status IS 'match: balances equal, mismatch: divergence detected';