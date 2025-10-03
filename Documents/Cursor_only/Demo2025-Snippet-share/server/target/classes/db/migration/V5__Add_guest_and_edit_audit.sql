ALTER TABLE snippets
  ADD COLUMN IF NOT EXISTS guest_session_id VARCHAR(64),
  ADD COLUMN IF NOT EXISTS last_edited_by_name VARCHAR(255),
  ADD COLUMN IF NOT EXISTS last_edited_at TIMESTAMP;

-- Optional index to accelerate guest filtering
CREATE INDEX IF NOT EXISTS idx_snippets_guest_session_id ON snippets(guest_session_id);

