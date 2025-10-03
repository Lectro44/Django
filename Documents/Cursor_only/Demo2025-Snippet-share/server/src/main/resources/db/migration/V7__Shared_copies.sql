ALTER TABLE snippets
  ADD COLUMN IF NOT EXISTS original_snippet_id UUID,
  ADD COLUMN IF NOT EXISTS shared_can_edit BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX IF NOT EXISTS idx_snippets_original_snippet_id ON snippets(original_snippet_id);

