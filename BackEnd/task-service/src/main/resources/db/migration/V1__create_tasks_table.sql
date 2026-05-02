-- V1__create_tasks_table.sql
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TYPE task_status AS ENUM ('TODO', 'IN_PROGRESS', 'IN_REVIEW', 'DONE', 'BLOCKED');
CREATE TYPE task_priority AS ENUM ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL');

CREATE TABLE tasks (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    title           VARCHAR(200)    NOT NULL,
    description     TEXT,
    status          task_status     NOT NULL DEFAULT 'TODO',
    priority        task_priority   NOT NULL DEFAULT 'MEDIUM',
    board_id        UUID            NOT NULL,
    team_id         UUID,
    assignee_id     UUID,
    reporter_id     UUID            NOT NULL,
    due_at          TIMESTAMPTZ,
    position_order  INTEGER         NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_tasks_board_status   ON tasks (board_id, status, position_order);
CREATE INDEX idx_tasks_assignee       ON tasks (assignee_id) WHERE assignee_id IS NOT NULL;
CREATE INDEX idx_tasks_team_id        ON tasks (team_id) WHERE team_id IS NOT NULL;
CREATE INDEX idx_tasks_reporter       ON tasks (reporter_id);

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_tasks_updated_at
    BEFORE UPDATE ON tasks
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
