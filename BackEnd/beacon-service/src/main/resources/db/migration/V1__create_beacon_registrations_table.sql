-- V1__create_beacon_registrations_table.sql
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE beacon_registrations (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID        NOT NULL,
    username        VARCHAR(50),
    local_ip        VARCHAR(45) NOT NULL,
    daemon_port     INTEGER     NOT NULL,
    team_id         UUID,
    last_seen       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    registered_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Unique per user — upsert (ON CONFLICT DO UPDATE) relies on this
CREATE UNIQUE INDEX idx_beacon_user_id   ON beacon_registrations (user_id);
CREATE INDEX        idx_beacon_last_seen ON beacon_registrations (last_seen);
CREATE INDEX        idx_beacon_team_id   ON beacon_registrations (team_id) WHERE team_id IS NOT NULL;
