package com.example.urlshortener.model;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

public class ShortUrl {
    private final Long id;
    private final String shortCode;
    private final String originalUrl;
    private final String userId;
    private final Instant createdAt;
    private final Instant expiresAt;
    private UrlStatus status;
    private long clickCount;
    private Instant lastAccessedAt;

    private ShortUrl(Builder builder) {
        this.id = builder.id;
        this.shortCode = Objects.requireNonNull(builder.shortCode, "shortCode cannot be null");
        this.originalUrl = Objects.requireNonNull(builder.originalUrl, "originalUrl cannot be null");
        this.userId = builder.userId;
        this.createdAt = Objects.requireNonNull(builder.createdAt, "createdAt cannot be null");
        this.expiresAt = Objects.requireNonNull(builder.expiresAt, "expiresAt cannot be null");
        this.status = Objects.requireNonNull(builder.status, "status cannot be null");
        this.clickCount = builder.clickCount;
        this.lastAccessedAt = builder.lastAccessedAt;

        if (!expiresAt.isAfter(createdAt)) {
            throw new IllegalArgumentException("expiresAt must be after createdAt");
        }
    }

    public boolean isExpired(Instant now) {
        return now.isAfter(this.expiresAt);
    }

    public synchronized void recordAccess(Instant accessTime) {
        this.clickCount++;
        this.lastAccessedAt = accessTime;
    }

    public void revoke() {
        this.status = UrlStatus.REVOKED;
    }

    public Long getId() { return id; }
    public String getShortCode() { return shortCode; }
    public String getOriginalUrl() { return originalUrl; }
    public Optional<String> getUserId() { return Optional.ofNullable(userId); }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public UrlStatus getStatus() { return status; }
    public synchronized long getClickCount() { return clickCount; }
    public synchronized Optional<Instant> getLastAccessedAt() { return Optional.ofNullable(lastAccessedAt); }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String shortCode;
        private String originalUrl;
        private String userId;
        private Instant createdAt;
        private Instant expiresAt;
        private UrlStatus status = UrlStatus.ACTIVE;
        private long clickCount = 0L;
        private Instant lastAccessedAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder shortCode(String shortCode) { this.shortCode = shortCode; return this; }
        public Builder originalUrl(String originalUrl) { this.originalUrl = originalUrl; return this; }
        public Builder userId(String userId) { this.userId = userId; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public Builder expiresAt(Instant expiresAt) { this.expiresAt = expiresAt; return this; }
        public Builder status(UrlStatus status) { this.status = status; return this; }
        public Builder clickCount(long clickCount) { this.clickCount = clickCount; return this; }
        public Builder lastAccessedAt(Instant lastAccessedAt) { this.lastAccessedAt = lastAccessedAt; return this; }

        public ShortUrl build() { return new ShortUrl(this); }
    }
}
