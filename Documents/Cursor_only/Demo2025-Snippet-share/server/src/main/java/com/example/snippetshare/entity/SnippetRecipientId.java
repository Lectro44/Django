package com.example.snippetshare.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class SnippetRecipientId implements Serializable {
    @Column(name = "snippet_id")
    private UUID snippetId;

    @Column(name = "user_id")
    private UUID userId;

    public SnippetRecipientId() {}

    public SnippetRecipientId(UUID snippetId, UUID userId) {
        this.snippetId = snippetId;
        this.userId = userId;
    }

    public UUID getSnippetId() { return snippetId; }
    public UUID getUserId() { return userId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SnippetRecipientId that = (SnippetRecipientId) o;
        return Objects.equals(snippetId, that.snippetId) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(snippetId, userId);
    }
}


