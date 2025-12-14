-- Migration: Create entries table (double-entry bookkeeping)
-- Purpose: Store ledger entries - source of truth for all balance calculations

CREATE TABLE entries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    operation_id UUID NOT NULL REFERENCES operations(id) ON DELETE RESTRICT,
    account_id UUID NOT NULL REFERENCES accounts(id) ON DELETE RESTRICT,
    amount NUMERIC(19, 4) NOT NULL CHECK (amount != 0),
    direction VARCHAR(10) NOT NULL CHECK (direction IN ('credit', 'debit')),
    entry_type VARCHAR(50) NOT NULL,
    source VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance (critical for balance calculations)
CREATE INDEX idx_entries_operation_id ON entries(operation_id);
CREATE INDEX idx_entries_account_id ON entries(account_id);
CREATE INDEX idx_entries_created_at ON entries(created_at);
CREATE INDEX idx_entries_account_created ON entries(account_id, created_at);

-- Comments for documentation
COMMENT ON TABLE entries IS 'Double-entry bookkeeping ledger - immutable entries only, NEVER updated or deleted';
COMMENT ON COLUMN entries.amount IS 'Can be positive or negative - CHECK ensures non-zero';
COMMENT ON COLUMN entries.direction IS 'credit (money in) or debit (money out)';
COMMENT ON COLUMN entries.entry_type IS 'Business context: initial_deposit, transfer_out, transfer_in, etc';
COMMENT ON COLUMN entries.source IS 'Origin of entry: bank_api, psp_webhook, internal, etc';