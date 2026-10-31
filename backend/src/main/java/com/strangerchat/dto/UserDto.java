package com.strangerchat.dto;

import java.time.Instant;
import java.util.UUID;

public class UserDto {
    private UUID id;
    private String username;
    private String email;
    private String role;
    private Instant createdAt;
    private Instant lastLoginAt;

    public UserDto() {}

    public UserDto(UUID id, String username, String email, String role, Instant createdAt, Instant lastLoginAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.createdAt = createdAt;
        this.lastLoginAt = lastLoginAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(Instant lastLoginAt) { this.lastLoginAt = lastLoginAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private UUID id;
        private String username;
        private String email;
        private String role;
        private Instant createdAt;
        private Instant lastLoginAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder username(String username) { this.username = username; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder role(String role) { this.role = role; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public Builder lastLoginAt(Instant lastLoginAt) { this.lastLoginAt = lastLoginAt; return this; }

        public UserDto build() {
            return new UserDto(id, username, email, role, createdAt, lastLoginAt);
        }
    }
}
