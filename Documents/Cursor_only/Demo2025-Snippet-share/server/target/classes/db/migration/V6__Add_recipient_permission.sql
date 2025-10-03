ALTER TABLE snippet_recipients
  ADD COLUMN IF NOT EXISTS can_write BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX IF NOT EXISTS idx_snippet_recipients_can_write ON snippet_recipients(can_write);

