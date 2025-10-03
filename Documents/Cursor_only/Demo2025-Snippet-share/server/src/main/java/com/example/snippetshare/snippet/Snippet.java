package com.example.snippetshare.snippet;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Snippet {
    private final String id;

    @NotBlank
    @Size(max = 120)
    private String title;

    @NotBlank
    private String code;

    @NotNull
    private Language language;

    private final Instant createdAt;

    public Snippet(String title, String code, Language language) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.code = code;
        this.language = language;
        this.createdAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Snippet snippet = (Snippet) o;
        return Objects.equals(id, snippet.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}


