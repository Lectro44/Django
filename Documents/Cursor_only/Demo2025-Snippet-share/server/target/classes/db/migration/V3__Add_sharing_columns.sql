-- Add sharing columns to snippets table
ALTER TABLE snippets ADD COLUMN is_shared BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE snippets ADD COLUMN shared_by_user_id UUID REFERENCES users(id) ON DELETE SET NULL;

-- Create index for shared snippets
CREATE INDEX idx_snippets_is_shared ON snippets(is_shared);
CREATE INDEX idx_snippets_shared_by_user_id ON snippets(shared_by_user_id);
