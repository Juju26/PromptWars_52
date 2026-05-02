-- V1__create_messaging_tables.sql
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TYPE channel_type AS ENUM ('TEAM', 'ANNOUNCEMENT', 'GENERAL');

CREATE TABLE channels (
    id          UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100)    NOT NULL,
    description VARCHAR(500),
    type        channel_type    NOT NULL DEFAULT 'TEAM',
    team_id     UUID,
    created_by  UUID            NOT NULL,
    created_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX idx_channels_name    ON channels (LOWER(name));
CREATE INDEX        idx_channels_team    ON channels (team_id) WHERE team_id IS NOT NULL;
CREATE INDEX        idx_channels_type    ON channels (type);

CREATE TABLE messages (
    id               UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    channel_id       UUID        NOT NULL REFERENCES channels(id) ON DELETE CASCADE,
    sender_id        UUID        NOT NULL,
    sender_username  VARCHAR(50),
    content          TEXT        NOT NULL,
    deleted_at       TIMESTAMPTZ,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Composite index for efficient cursor pagination (channel + time)
CREATE INDEX idx_messages_channel_cursor ON messages (channel_id, created_at DESC)
    WHERE deleted_at IS NULL;
CREATE INDEX idx_messages_sender         ON messages (sender_id);
