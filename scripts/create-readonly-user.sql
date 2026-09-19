-- ==============================================================================
-- TradeX Production: Create Dedicated Read-Only User
-- Use this role for day-to-day data inspection and debugging in DBeaver / DataGrip.
-- This physically prevents accidental UPDATE, DELETE, TRUNCATE, or DROP operations.
--
-- How to run:
--   docker compose -f docker-compose-prod.yml exec -T postgres \
--     psql -U tradex -d tradex < scripts/create-readonly-user.sql
-- ==============================================================================

-- 1. Create the read-only user (replace password with a secure password!)
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'tradex_readonly') THEN
        CREATE ROLE tradex_readonly WITH LOGIN PASSWORD 'ChangeMeReadOnlyPass123!';
        RAISE NOTICE 'Role tradex_readonly created successfully.';
    ELSE
        RAISE NOTICE 'Role tradex_readonly already exists.';
    END IF;
END
$$;

-- 2. Grant connection and schema usage
GRANT CONNECT ON DATABASE tradex TO tradex_readonly;
GRANT USAGE ON SCHEMA public TO tradex_readonly;

-- 3. Grant SELECT privileges on all current tables and sequences
GRANT SELECT ON ALL TABLES IN SCHEMA public TO tradex_readonly;
GRANT SELECT ON ALL SEQUENCES IN SCHEMA public TO tradex_readonly;

-- 4. Automatically grant SELECT on any future tables created by migrations
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO tradex_readonly;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON SEQUENCES TO tradex_readonly;

