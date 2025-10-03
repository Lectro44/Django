package com.example.snippetshare.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "snippet_recipients")
public class SnippetRecipient {

    @EmbeddedId
    private SnippetRecipientId id = new SnippetRecipientId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("snippetId")
    @JoinColumn(name = "snippet_id")
    private SnippetEntity snippet;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "can_write", nullable = false)
    private boolean canWrite = false;

    public SnippetRecipient() {}

    public SnippetRecipient(SnippetEntity snippet, UserEntity user) {
        this.snippet = snippet;
        this.user = user;
        this.id = new SnippetRecipientId(snippet.getId(), user.getId());
    }

    public SnippetRecipientId getId() { return id; }
    public SnippetEntity getSnippet() { return snippet; }
    public UserEntity getUser() { return user; }
    public Instant getCreatedAt() { return createdAt; }
    public boolean isCanWrite() { return canWrite; }
    public void setCanWrite(boolean canWrite) { this.canWrite = canWrite; }
}


