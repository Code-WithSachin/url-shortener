package com.example.urlshortener.dto;

import com.example.urlshortener.model.UrlStatus;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

public class UrlStatsResponse {
    private final String shortCode;
    private final String originalUrl;
    private final long clickCount;
    private final Instant createdAt;
    private final Instant expiresAt;
    private final Instant lastAccessedAt;
    private final UrlStatus status;

    public UrlStatsResponse(
            String shortCode,
            String originalUrl,
            long clickCount,
            Instant createdAt,
            Instant expiresAt,
            Instant lastAccessedAt,
            UrlStatus status) {
        this.shortCode = Objects.requireNonNull(shortCode, "shortCode cannot be null");
        this.originalUrl = Objects.requireNonNull(originalUrl, "originalUrl cannot be null");
        this.clickCount = clickCount;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt cannot be null");
        this.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt cannot be null");
        this.lastAccessedAt = lastAccessedAt;
        this.status = Objects.requireNonNull(status, "status cannot be null");
    }

    public String getShortCode() {
        return shortCode;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public long getClickCount() {
        return clickCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Optional<Instant> getLastAccessedAt() {
        return Optional.ofNullable(lastAccessedAt);
    }

    public UrlStatus getStatus() {
        return status;
    }
}
