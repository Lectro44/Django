package com.example.snippetshare.user;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.UUID;

public class User {
    private final String id;
    @NotBlank
    private String name;
    @NotBlank
    private String password;
    private final Instant createdAt;

    public User(String name, String password) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.password = password;
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Instant getCreatedAt() { return createdAt; }
}


