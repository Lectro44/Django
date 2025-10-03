-- Create a convenience view for guest snippets (those without a user)
CREATE OR REPLACE VIEW guest_snippets AS
SELECT * FROM snippets WHERE user_id IS NULL;


