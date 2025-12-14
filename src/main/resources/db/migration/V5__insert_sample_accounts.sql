-- Migration: Insert sample accounts for testing
-- Purpose: Create test accounts with known UUIDs for easy testing

INSERT INTO accounts (id, type, created_at) VALUES
    ('11111111-1111-1111-1111-111111111111', 'user', CURRENT_TIMESTAMP),
    ('22222222-2222-2222-2222-222222222222', 'user', CURRENT_TIMESTAMP),
    ('33333333-3333-3333-3333-333333333333', 'system', CURRENT_TIMESTAMP);

-- Comments
COMMENT ON TABLE accounts IS 'Sample accounts created for testing and demonstration purposes';