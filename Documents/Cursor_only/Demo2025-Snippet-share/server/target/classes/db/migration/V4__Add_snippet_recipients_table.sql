-- Recipients mapping table for sharing snippets with multiple users
CREATE TABLE IF NOT EXISTS snippet_recipients (
    snippet_id UUID NOT NULL REFERENCES snippets(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (snippet_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_snippet_recipients_user_id ON snippet_recipients(user_id);
CREATE INDEX IF NOT EXISTS idx_snippet_recipients_snippet_id ON snippet_recipients(snippet_id);


