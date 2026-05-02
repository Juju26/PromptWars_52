-- V1__create_users_table.sql
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username        VARCHAR(50)  NOT NULL,
    email           VARCHAR(255) NOT NULL,
    password_hash   TEXT         NOT NULL,
    role            VARCHAR(20)  NOT NULL DEFAULT 'PARTICIPANT',
    display_name    VARCHAR(100),
    team_id         UUID,
    enabled         BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX idx_users_email    ON users (LOWER(email));
CREATE UNIQUE INDEX idx_users_username ON users (LOWER(username));
CREATE INDEX        idx_users_team_id  ON users (team_id) WHERE team_id IS NOT NULL;

-- Auto-update updated_at via trigger
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
