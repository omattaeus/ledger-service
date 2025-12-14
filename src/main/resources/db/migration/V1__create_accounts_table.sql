-- Migration: Create accounts table
-- Purpose: Store account information WITHOUT balance column (principle #1: balance is calculated, not stored)

CREATE TABLE accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    type VARCHAR(20) NOT NULL CHECK (type IN ('user', 'system', 'transit')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_accounts_type ON accounts(type);
CREATE INDEX idx_accounts_created_at ON accounts(created_at);

-- Comments for documentation
COMMENT ON TABLE accounts IS 'Account entities - NO balance column (balance is calculated from entries)';
COMMENT ON COLUMN accounts.type IS 'Account type: user (end users), system (internal), transit (temporary)';