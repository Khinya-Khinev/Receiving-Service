ALTER TABLE worker_receiving_sessions ADD COLUMN started_at TIMESTAMP NOT NULL DEFAULT NOW();
ALTER TABLE worker_receiving_sessions ADD COLUMN completed_at TIMESTAMP;
