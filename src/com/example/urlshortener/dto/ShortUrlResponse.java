package com.example.urlshortener.dto;

import java.time.Instant;
import java.util.Objects;

public class ShortUrlResponse {
    private final String shortCode;
    private final String fullShortUrl;
    private final String originalUrl;
    private final Instant expiresAt;

    public ShortUrlResponse(String shortCode, String fullShortUrl, String originalUrl, Instant expiresAt) {
        this.shortCode = Objects.requireNonNull(shortCode, "shortCode cannot be null");
        this.fullShortUrl = Objects.requireNonNull(fullShortUrl, "fullShortUrl cannot be null");
        this.originalUrl = Objects.requireNonNull(originalUrl, "originalUrl cannot be null");
        this.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt cannot be null");
    }

    public String getShortCode() {
        return shortCode;
    }

    public String getFullShortUrl() {
        return fullShortUrl;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}
